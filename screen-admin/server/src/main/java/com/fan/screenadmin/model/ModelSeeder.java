package com.fan.screenadmin.model;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ModelSeeder implements ApplicationRunner {

    private final ModelRepository repository;
    private final ModelService modelService;
    private final ModelProgressRepository progressRepository;

    public ModelSeeder(
            ModelRepository repository,
            ModelService modelService,
            ModelProgressRepository progressRepository
    ) {
        this.repository = repository;
        this.modelService = modelService;
        this.progressRepository = progressRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() == 0) {
            modelService.create(new ModelDtos.ModelRequest(
                    "预应力混凝土梁桥",
                    "桥梁",
                    "/platform/works/layout-04.png",
                    "/platform/models/bridge-sample.glb",
                    "内置示例桥梁模型，用于验证卡片列表、GLB 加载和三维交互。可在后台修改或下架。",
                    List.of("桥梁", "GLB", "示例"),
                    "published",
                    1
            ));
        }
        seedWuhanYangtzeRiverBridge();
        seedYingwuzhouYangtzeRiverBridge();
        seedHankouChangfengBridge();
        seedHouseTour();
        seedVillaModel();
        seedSuzhouGarden();
        seedWuhanProgress();
    }

    private void seedWuhanYangtzeRiverBridge() {
        String title = "武汉长江大桥";
        if (repository.existsByTitle(title)) {
            return;
        }
        modelService.create(new ModelDtos.ModelRequest(
                title,
                "桥梁",
                "/platform/works/layout-04.png",
                "/platform/models/wuhan-yangtze-river-bridge.glb",
                "参考武汉长江大桥双层钢桁梁结构生成的程序化 Three.js 模型，包含公路层、铁路层、钢桁架、桥墩与桥头堡。",
                List.of("武汉", "长江大桥", "Three.js", "钢桁梁"),
                "published",
                2
        ));
    }

    private void seedYingwuzhouYangtzeRiverBridge() {
        String title = "鹦鹉洲长江大桥";
        if (repository.existsByTitle(title)) {
            return;
        }
        modelService.create(new ModelDtos.ModelRequest(
                title,
                "桥梁",
                "/platform/works/layout-04.png",
                "/platform/models/yingwuzhou-yangtze-river-bridge.glb",
                "参考鹦鹉洲长江大桥三塔四跨悬索结构生成的程序化 Three.js 模型，包含橙红色钢箱梁、三座桥塔、主缆、吊索与引桥。",
                List.of("武汉", "鹦鹉洲", "Three.js", "悬索桥"),
                "published",
                3
        ));
    }

    private void seedHankouChangfengBridge() {
        String title = "武汉市汉口长丰桥";
        String description = "参考武汉长丰桥设计的盛夏蓝中承式钢管混凝土系杆拱桥模型，按主跨 252m、边拱 60m、桥宽 27m 与双向六车道比例构建。";
        String tags = "武汉,汉口,长丰桥,Three.js,盛夏蓝,系杆拱桥";
        repository.updateDescriptionAndTagsByTitle(title, description, tags);
        if (repository.existsByTitle(title)) {
            return;
        }
        repository.deleteByTitle("武昌长丰桥");
        modelService.create(new ModelDtos.ModelRequest(
                title,
                "桥梁",
                "/platform/works/layout-04.png",
                "/platform/models/hankou-changfeng-bridge.glb",
                description,
                List.of("武汉", "汉口", "长丰桥", "Three.js", "盛夏蓝", "系杆拱桥"),
                "published",
                4
        ));
    }

    private void seedWuhanProgress() {
        String modelId = repository.findIdByTitle("武汉长江大桥").orElse(null);
        if (modelId == null || progressRepository.countByModel(modelId) > 0) {
            return;
        }

        String unitId = "wuhan-unit-01";
        List<ModelProgressDtos.NodeRequest> nodes = new ArrayList<>();
        nodes.add(new ModelProgressDtos.NodeRequest(
                unitId, null, "unit", "第一联", null, "in_progress",
                null, null, "2026-01-05", "2027-04-30",
                "2026-01-05", null, "桥梁项目部", null,
                "主桥共9跨，每跨包含钢桁梁、桥面板和下层铁路梁。", 1
        ));
        for (int spanNo = 1; spanNo <= 9; spanNo += 1) {
            String spanName = "第" + spanNo + "跨";
            String spanId = unitId + "-span-" + spanNo;
            String spanStatus = spanNo <= 5 ? "completed" : spanNo == 6 ? "in_progress" : "not_started";
            String plannedEnd = LocalDate.parse("2026-11-05").plusDays(spanNo * 30L).toString();
            nodes.add(new ModelProgressDtos.NodeRequest(
                    spanId, unitId, "span", spanName, null, spanStatus,
                    null, 3.0, "2026-01-05", plannedEnd,
                    "2026-01-05", spanNo <= 5 ? plannedEnd : null,
                    "桥梁项目部", null, null, spanNo + 1
            ));
            for (String[] member : new String[][]{
                    {"girder", "钢桁梁"},
                    {"deck", "桥面板"},
                    {"lower-beam", "下层铁路梁"}
            }) {
                String key = "Unit-01.Span-%02d.%s".formatted(spanNo, member[0]);
                String status = spanNo <= 5
                        ? "completed"
                        : spanNo == 6
                        ? switch (member[0]) {
                            case "girder" -> "completed";
                            case "deck" -> "in_progress";
                            default -> "delayed";
                        }
                        : "not_started";
                nodes.add(new ModelProgressDtos.NodeRequest(
                        spanId + "-" + member[0], spanId, "member", member[1], key, status,
                        null, 1.0, "2026-01-05", plannedEnd,
                        "2026-01-05", spanNo <= 5 || (spanNo == 6 && "girder".equals(member[0]))
                                ? plannedEnd
                                : null,
                        "桥梁班组" + spanNo,
                        "delayed".equals(status) ? "钢结构供货延迟" : null,
                        "数据驱动示例节点", spanNo * 10
                ));
            }
        }
        progressRepository.replace(modelId, nodes.stream().map(node -> new ModelProgressRepository.ProgressInput(
                node.id(),
                node.parentId(),
                node.nodeType(),
                node.name(),
                node.meshKey(),
                node.status(),
                node.progressPercent() == null
                        ? switch (node.status()) {
                            case "completed" -> 100;
                            case "in_progress", "delayed" -> 50;
                            default -> 0;
                        }
                        : node.progressPercent(),
                node.weight() == null ? 1.0 : node.weight(),
                node.plannedStartDate(),
                node.plannedEndDate(),
                node.actualStartDate(),
                node.actualEndDate(),
                node.owner(),
                node.delayReason(),
                node.remark(),
                node.sortNo()
        )).toList());
        progressRepository.insertSnapshot(modelId, progressRepository.list(modelId));
        progressRepository.audit(modelId, "seed", "初始化武汉长江大桥进度数据");
    }

    private void seedHouseTour() {
        String title = "三室两厅 · 户型漫游";
        if (repository.existsByTitle(title)) {
            return;
        }
        modelService.create(new ModelDtos.ModelRequest(
                title,
                "户型",
                "/platform/models/house-tour.png",
                "/platform/models/house-tour",
                "三室两厅结构示意图，约109㎡，包含3室2厅1厨1卫。支持全屋俯视、室内环视、房间切换与门口热点导航。",
                List.of("户型", "三室两厅", "Three.js", "室内漫游"),
                "published",
                5
        ));
    }

    private void seedVillaModel() {
        String title = "新中式三层别墅";
        if (repository.existsByTitle(title)) {
            return;
        }
        modelService.create(new ModelDtos.ModelRequest(
                title,
                "别墅",
                "/platform/models/villa.png",
                "/platform/models/villa",
                "新中式三层别墅三维漫游，包含庭院、长方形无边水景池、双车位车库与三层重点室内空间。默认展示晴天外观，可切换庭院、泳池、车库和楼层视角。",
                List.of("别墅", "新中式", "Three.js", "庭院", "无边泳池", "双车位车库"),
                "published",
                6
        ));
    }

    private void seedSuzhouGarden() {
        String title = "苏州拙政园 · 中部园景";
        String description = "参考拙政园中部构建的沉浸式三维园景，包含远香堂、小飞虹廊桥、荷风四面亭、香洲、水面、荷花、假山、树木与江南远山。支持固定视角与自动漫游。";
        repository.updateDescriptionAndTagsByTitle(title, description, "苏州,拙政园,Three.js,远香堂,小飞虹,长廊,水面,假山");
        if (repository.existsByTitle(title)) {
            return;
        }
        modelService.create(new ModelDtos.ModelRequest(
                title,
                "园林",
                "/platform/models/suzhou-garden.png",
                "/platform/models/suzhou-garden",
                description,
                List.of("苏州", "拙政园", "Three.js", "远香堂", "小飞虹", "长廊", "水面", "假山"),
                "published",
                7
        ));
    }
}
