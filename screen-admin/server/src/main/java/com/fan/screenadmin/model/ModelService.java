package com.fan.screenadmin.model;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class ModelService {

    private static final Set<String> STATUSES = Set.of("published", "draft");

    private final ModelRepository repository;
    private final ModelProgressRepository progressRepository;

    public ModelService(ModelRepository repository, ModelProgressRepository progressRepository) {
        this.repository = repository;
        this.progressRepository = progressRepository;
    }

    public ModelDtos.ModelPageResponse list(
            String keyword,
            String category,
            Integer page,
            Integer pageSize,
            boolean publishedOnly
    ) {
        int normalizedPage = normalizePositive(page, 1);
        int normalizedPageSize = Math.min(normalizePositive(pageSize, 9), 48);
        var result = repository.page(new ModelRepository.ModelQuery(
                normalizeKeyword(keyword),
                normalizeKeyword(category),
                publishedOnly,
                normalizedPageSize,
                (normalizedPage - 1) * normalizedPageSize
        ));
        List<ModelDtos.ModelResponse> items = result.rows().stream().map(this::map).toList();
        return new ModelDtos.ModelPageResponse(items, result.total(), normalizedPage, normalizedPageSize);
    }

    public List<ModelDtos.ModelResponse> listAll() {
        return repository.page(new ModelRepository.ModelQuery(null, null, false, 10000, 0))
                .rows()
                .stream()
                .map(this::map)
                .toList();
    }

    public ModelDtos.ModelFacetResponse facets() {
        return new ModelDtos.ModelFacetResponse(repository.categories());
    }

    public ModelDtos.ModelResponse create(ModelDtos.ModelRequest request) {
        ModelRepository.ModelInput input = validateAndNormalize(request);
        String id = UUID.randomUUID().toString();
        int sortNo = request.sortNo() == null ? repository.nextSortNo() : request.sortNo();
        repository.insert(withSortNo(input, sortNo), id);
        return get(id);
    }

    public ModelDtos.ModelResponse update(String id, ModelDtos.ModelRequest request) {
        ensure(id);
        ModelRepository.ModelInput input = validateAndNormalize(request);
        int sortNo = request.sortNo() == null ? repository.find(id).orElseThrow().sortNo() : request.sortNo();
        repository.update(id, withSortNo(input, sortNo));
        return get(id);
    }

    public void delete(String id) {
        progressRepository.deleteByModel(id);
        if (repository.delete(id) == 0) {
            throw new IllegalArgumentException("模型不存在");
        }
    }

    public ModelDtos.ModelResponse get(String id) {
        return repository.find(id).map(this::map)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));
    }

    public ModelDtos.ModelResponse getPublished(String id) {
        ModelRepository.ModelRow row = repository.find(id)
                .filter(item -> Objects.equals(item.status(), "published"))
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));
        return map(row);
    }

    private ModelRepository.ModelInput validateAndNormalize(ModelDtos.ModelRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("模型数据不能为空");
        }
        String title = requireText(request.title(), "模型名称", 150);
        String category = requireText(request.category(), "模型分类", 50);
        String desc = cleanText(request.desc(), 2000);
        String cover = validateUrl(request.cover(), "封面地址", true);
        String fileUrl = validateUrl(request.fileUrl(), "模型文件地址", false);
        String status = Objects.requireNonNullElse(request.status(), "draft").toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(status)) {
            throw new IllegalArgumentException("发布状态不合法");
        }
        if ("published".equals(status) && (fileUrl == null || fileUrl.isBlank())) {
            throw new IllegalArgumentException("发布前必须填写模型文件");
        }
        List<String> tags = request.tags() == null ? List.of() : request.tags().stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> cleanText(tag, 30))
                .distinct()
                .limit(10)
                .toList();
        return new ModelRepository.ModelInput(
                title,
                category,
                cover,
                fileUrl,
                desc,
                String.join(",", tags),
                status,
                0
        );
    }

    private ModelRepository.ModelInput withSortNo(ModelRepository.ModelInput input, int sortNo) {
        return new ModelRepository.ModelInput(
                input.title(),
                input.category(),
                input.cover(),
                input.fileUrl(),
                input.desc(),
                input.tags(),
                input.status(),
                sortNo
        );
    }

    private ModelDtos.ModelResponse map(ModelRepository.ModelRow row) {
        List<String> tags = row.tags() == null || row.tags().isBlank()
                ? List.of()
                : List.of(row.tags().split(","));
        return new ModelDtos.ModelResponse(
                row.id(),
                row.title(),
                row.category(),
                row.cover(),
                row.fileUrl(),
                row.desc(),
                tags,
                row.status(),
                row.updatedAt()
        );
    }

    private void ensure(String id) {
        if (repository.find(id).isEmpty()) {
            throw new IllegalArgumentException("模型不存在");
        }
    }

    private String requireText(String value, String label, int maxLength) {
        String result = cleanText(value, maxLength);
        if (result == null || result.isBlank()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        return result;
    }

    String cleanText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.length() > maxLength ? result.substring(0, maxLength) : result;
    }

    private String validateUrl(String value, String label, boolean optional) {
        String result = cleanText(value, 1000);
        if (result == null || result.isBlank()) {
            if (optional) {
                return null;
            }
            throw new IllegalArgumentException(label + "不能为空");
        }
        if (result.startsWith("/uploads/") || result.startsWith("/platform/")) {
            return result;
        }
        try {
            URI uri = URI.create(result);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(label + "必须是 http、https 或本站上传地址");
            }
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null && exception.getMessage().startsWith(label)) {
                throw exception;
            }
            throw new IllegalArgumentException(label + "格式不合法");
        }
        return result;
    }

    private String normalizeKeyword(String value) {
        String result = cleanText(value, 100);
        return result == null || result.isBlank() ? null : result.toLowerCase();
    }

    private int normalizePositive(Integer value, int defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }
}
