package com.fan.screenadmin.model;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class ModelProgressService {

    private static final Set<String> NODE_TYPES = Set.of("bridge", "unit", "span", "member");
    private static final Set<String> STATUSES = Set.of("not_started", "in_progress", "completed", "delayed");

    private final ModelRepository modelRepository;
    private final ModelProgressRepository repository;

    public ModelProgressService(ModelRepository modelRepository, ModelProgressRepository repository) {
        this.modelRepository = modelRepository;
        this.repository = repository;
    }

    public ModelProgressDtos.TreeResponse tree(String modelId, boolean publishedOnly) {
        ensureModel(modelId, publishedOnly);
        List<ModelProgressRepository.ProgressRow> rows = repository.list(modelId);
        return new ModelProgressDtos.TreeResponse(
                buildTree(rows),
                summary(modelId, rows),
                rows.stream()
                        .filter(row -> "member".equals(row.nodeType()) && isBlank(row.meshKey()))
                        .map(ModelProgressRepository.ProgressRow::name)
                        .toList()
        );
    }

    public ModelProgressDtos.SummaryResponse summary(String modelId, boolean publishedOnly) {
        ensureModel(modelId, publishedOnly);
        return summary(modelId, repository.list(modelId));
    }

    @Transactional
    public ModelProgressDtos.TreeResponse save(String modelId, ModelProgressDtos.SaveRequest request) {
        ensureModel(modelId, false);
        repository.replace(modelId, normalize(request));
        List<ModelProgressRepository.ProgressRow> rows = repository.list(modelId);
        repository.insertSnapshot(modelId, rows);
        repository.audit(modelId, "save", "保存 " + rows.size() + " 个进度节点");
        return tree(modelId, false);
    }

    @Transactional
    public ModelProgressDtos.TreeResponse importCsv(String modelId, String csv) {
        ModelProgressDtos.TreeResponse result = save(modelId, parseCsv(csv));
        repository.audit(modelId, "import", "CSV 导入 " + result.summary().total() + " 个叶子节点");
        return result;
    }

    public List<ModelProgressDtos.SnapshotResponse> snapshots(String modelId, boolean publishedOnly) {
        ensureModel(modelId, publishedOnly);
        return repository.snapshots(modelId);
    }

    public List<ModelProgressDtos.AuditResponse> audits(String modelId) {
        ensureModel(modelId, false);
        return repository.audits(modelId);
    }

    public void deleteByModel(String modelId) {
        repository.deleteByModel(modelId);
    }

    private List<ModelProgressRepository.ProgressInput> normalize(ModelProgressDtos.SaveRequest request) {
        if (request == null || request.nodes() == null || request.nodes().isEmpty()) {
            throw new IllegalArgumentException("进度节点不能为空");
        }
        if (request.nodes().size() > 1000) {
            throw new IllegalArgumentException("进度节点最多1000个");
        }

        Map<String, String> ids = new LinkedHashMap<>();
        for (ModelProgressDtos.NodeRequest node : request.nodes()) {
            if (isBlank(node.name())) {
                throw new IllegalArgumentException("进度节点名称不能为空");
            }
            String incomingId = isBlank(node.id()) ? UUID.randomUUID().toString() : node.id().trim();
            if (ids.put(incomingId, incomingId) != null) {
                throw new IllegalArgumentException("进度节点ID重复：" + incomingId);
            }
        }

        Map<String, String> names = new HashMap<>();
        request.nodes().forEach(node -> {
            if (!isBlank(node.id())) {
                names.put(clean(node.name(), 150), node.id().trim());
            }
        });

        List<ModelProgressRepository.ProgressInput> inputs = new ArrayList<>();
        for (int index = 0; index < request.nodes().size(); index += 1) {
            ModelProgressDtos.NodeRequest node = request.nodes().get(index);
            String id = isBlank(node.id()) ? UUID.randomUUID().toString() : node.id().trim();
            String parentId = null;
            if (!isBlank(node.parentId())) {
                String parentReference = node.parentId().trim();
                parentId = ids.containsKey(parentReference)
                        ? ids.get(parentReference)
                        : names.get(parentReference);
                if (parentId == null) {
                    throw new IllegalArgumentException("父级节点不存在：" + parentReference);
                }
            }
            String status = normalizeStatus(node.status());
            inputs.add(new ModelProgressRepository.ProgressInput(
                    id,
                    parentId,
                    normalizeNodeType(node.nodeType()),
                    clean(node.name(), 150),
                    clean(node.meshKey(), 200),
                    status,
                    normalizeProgress(node.progressPercent(), status),
                    normalizeWeight(node.weight()),
                    date(node.plannedStartDate(), "计划开始日期"),
                    date(node.plannedEndDate(), "计划完成日期"),
                    date(node.actualStartDate(), "实际开始日期"),
                    date(node.actualEndDate(), "实际完成日期"),
                    clean(node.owner(), 100),
                    clean(node.delayReason(), 500),
                    clean(node.remark(), 1000),
                    node.sortNo() == null ? index + 1 : node.sortNo()
            ));
        }
        ensureNoCycle(inputs);
        return inputs;
    }

    private void ensureNoCycle(List<ModelProgressRepository.ProgressInput> inputs) {
        Map<String, ModelProgressRepository.ProgressInput> byId = new HashMap<>();
        inputs.forEach(input -> byId.put(input.id(), input));
        for (ModelProgressRepository.ProgressInput input : inputs) {
            Set<String> visited = new HashSet<>();
            String current = input.id();
            while (current != null) {
                if (!visited.add(current)) {
                    throw new IllegalArgumentException("进度节点父级关系不能形成循环");
                }
                ModelProgressRepository.ProgressInput next = byId.get(current);
                current = next == null ? null : next.parentId();
            }
        }
    }

    private List<ModelProgressDtos.NodeResponse> buildTree(List<ModelProgressRepository.ProgressRow> rows) {
        Map<String, List<ModelProgressRepository.ProgressRow>> children = new LinkedHashMap<>();
        rows.forEach(row -> children
                .computeIfAbsent(row.parentId() == null ? "" : row.parentId(), key -> new ArrayList<>())
                .add(row));
        return children.getOrDefault("", List.of()).stream()
                .map(row -> toResponse(row, children))
                .toList();
    }

    private ModelProgressDtos.NodeResponse toResponse(
            ModelProgressRepository.ProgressRow row,
            Map<String, List<ModelProgressRepository.ProgressRow>> children
    ) {
        return new ModelProgressDtos.NodeResponse(
                row.id(),
                row.parentId(),
                row.nodeType(),
                row.name(),
                row.meshKey(),
                row.status(),
                displayStatus(row, LocalDate.now()),
                row.progressPercent(),
                row.weight(),
                row.plannedStartDate(),
                row.plannedEndDate(),
                row.actualStartDate(),
                row.actualEndDate(),
                row.owner(),
                row.delayReason(),
                row.remark(),
                children.getOrDefault(row.id(), List.of()).stream()
                        .map(child -> toResponse(child, children))
                        .toList()
        );
    }

    private ModelProgressDtos.SummaryResponse summary(
            String modelId,
            List<ModelProgressRepository.ProgressRow> rows
    ) {
        List<ModelProgressRepository.ProgressRow> leaves = leaves(rows);
        int completed = 0;
        int inProgress = 0;
        int notStarted = 0;
        int delayed = 0;
        double weightedProgress = 0;
        double weightTotal = 0;
        for (ModelProgressRepository.ProgressRow row : leaves) {
            String status = displayStatus(row, LocalDate.now());
            switch (status) {
                case "completed" -> completed += 1;
                case "in_progress" -> inProgress += 1;
                case "delayed" -> delayed += 1;
                default -> notStarted += 1;
            }
            weightedProgress += row.progressPercent() * row.weight();
            weightTotal += row.weight();
        }
        int progressPercent = weightTotal == 0 ? 0 : (int) Math.round(weightedProgress / weightTotal);
        String currentStage = leaves.stream()
                .min(currentStageOrder())
                .map(row -> row.name() + switch (displayStatus(row, LocalDate.now())) {
                    case "delayed" -> "（延期）";
                    case "in_progress" -> "（施工中）";
                    case "completed" -> "（已完成）";
                    default -> "（未开始）";
                })
                .orElse("暂无进度数据");
        return new ModelProgressDtos.SummaryResponse(
                leaves.size(),
                completed,
                inProgress,
                notStarted,
                delayed,
                progressPercent,
                currentStage,
                repository.latestUpdatedAt(modelId).map(Object::toString).orElse(null)
        );
    }

    private static Comparator<ModelProgressRepository.ProgressRow> currentStageOrder() {
        return Comparator.<ModelProgressRepository.ProgressRow>comparingInt(row -> switch (displayStatus(row, LocalDate.now())) {
            case "delayed" -> 0;
            case "in_progress" -> 1;
            case "completed" -> 2;
            default -> 3;
        }).thenComparing(ModelProgressRepository.ProgressRow::name);
    }

    private List<ModelProgressRepository.ProgressRow> leaves(List<ModelProgressRepository.ProgressRow> rows) {
        Set<String> parentIds = new HashSet<>();
        rows.forEach(row -> {
            if (row.parentId() != null) {
                parentIds.add(row.parentId());
            }
        });
        return rows.stream().filter(row -> !parentIds.contains(row.id())).toList();
    }

    private static String displayStatus(ModelProgressRepository.ProgressRow row, LocalDate today) {
        if ("delayed".equals(row.status())) {
            return "delayed";
        }
        if (!"completed".equals(row.status()) && row.plannedEndDate() != null
                && LocalDate.parse(row.plannedEndDate()).isBefore(today)) {
            return "delayed";
        }
        return row.status();
    }

    private ModelProgressDtos.SaveRequest parseCsv(String csv) {
        if (isBlank(csv)) {
            throw new IllegalArgumentException("CSV 内容不能为空");
        }
        List<List<String>> table = parseCsvTable(csv.strip());
        if (table.size() < 2) {
            throw new IllegalArgumentException("CSV 至少包含表头和一行数据");
        }
        Map<String, Integer> headers = new HashMap<>();
        for (int index = 0; index < table.getFirst().size(); index += 1) {
            headers.put(table.getFirst().get(index).replace("\uFEFF", "").trim(), index);
        }
        List<ModelProgressDtos.NodeRequest> nodes = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < table.size(); rowIndex += 1) {
            List<String> columns = table.get(rowIndex);
            nodes.add(new ModelProgressDtos.NodeRequest(
                    null,
                    csvValue(headers, columns, "parentId"),
                    csvValue(headers, columns, "nodeType"),
                    csvValue(headers, columns, "name"),
                    csvValue(headers, columns, "meshKey"),
                    csvValue(headers, columns, "status"),
                    integer(csvValue(headers, columns, "progressPercent"), "完成率"),
                    doubleValue(csvValue(headers, columns, "weight"), "权重"),
                    csvValue(headers, columns, "plannedStartDate"),
                    csvValue(headers, columns, "plannedEndDate"),
                    csvValue(headers, columns, "actualStartDate"),
                    csvValue(headers, columns, "actualEndDate"),
                    csvValue(headers, columns, "owner"),
                    csvValue(headers, columns, "delayReason"),
                    csvValue(headers, columns, "remark"),
                    nodes.size() + 1
            ));
        }
        return new ModelProgressDtos.SaveRequest(nodes);
    }

    private static List<List<String>> parseCsvTable(String csv) {
        List<List<String>> result = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < csv.length(); index += 1) {
            char character = csv.charAt(index);
            if (quoted) {
                if (character == '"') {
                    if (index + 1 < csv.length() && csv.charAt(index + 1) == '"') {
                        cell.append('"');
                        index += 1;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(character);
                }
            } else if (character == '"') {
                quoted = true;
            } else if (character == ',') {
                row.add(cell.toString());
                cell.setLength(0);
            } else if (character == '\n') {
                row.add(cell.toString());
                result.add(row);
                row = new ArrayList<>();
                cell.setLength(0);
            } else if (character != '\r') {
                cell.append(character);
            }
        }
        row.add(cell.toString());
        result.add(row);
        return result;
    }

    private static String csvValue(Map<String, Integer> headers, List<String> columns, String name) {
        Integer index = headers.get(name);
        return index == null || index >= columns.size() ? null : columns.get(index);
    }

    private void ensureModel(String modelId, boolean publishedOnly) {
        var model = modelRepository.find(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));
        if (publishedOnly && !"published".equals(model.status())) {
            throw new IllegalArgumentException("模型不存在");
        }
    }

    private String normalizeNodeType(String value) {
        String result = Objects.requireNonNullElse(value, "member").toLowerCase(Locale.ROOT);
        if (!NODE_TYPES.contains(result)) {
            throw new IllegalArgumentException("进度节点类型不合法");
        }
        return result;
    }

    private String normalizeStatus(String value) {
        String result = Objects.requireNonNullElse(value, "not_started").toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(result)) {
            throw new IllegalArgumentException("进度状态不合法");
        }
        return result;
    }

    private int normalizeProgress(Integer value, String status) {
        int result = value == null
                ? switch (status) {
            case "completed" -> 100;
            case "in_progress", "delayed" -> 50;
            default -> 0;
        }
                : value;
        if (result < 0 || result > 100) {
            throw new IllegalArgumentException("完成率必须在0到100之间");
        }
        return result;
    }

    private double normalizeWeight(Double value) {
        double result = value == null ? 1 : value;
        if (result <= 0 || result > 100000) {
            throw new IllegalArgumentException("权重必须大于0且不超过100000");
        }
        return result;
    }

    private String date(String value, String label) {
        String result = value == null ? null : value.trim();
        if (result == null || result.isEmpty()) {
            return null;
        }
        if (result.length() > 10) {
            result = result.substring(0, 10);
        }
        try {
            return LocalDate.parse(result).toString();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(label + "格式必须为YYYY-MM-DD");
        }
    }

    private Integer integer(String value, String label) {
        if (isBlank(value)) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + "必须是数字");
        }
    }

    private Double doubleValue(String value, String label) {
        if (isBlank(value)) return null;
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + "必须是数字");
        }
    }

    private static String clean(String value, int maxLength) {
        if (value == null) return null;
        String result = value.trim();
        return result.length() > maxLength ? result.substring(0, maxLength) : result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
