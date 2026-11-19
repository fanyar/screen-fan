package com.fan.screenadmin.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> LEGACY_ARTICLE_TITLES = List.of(
            "Vue 3 组合式 API 在大屏项目中的实践",
            "Tailwind + pxtorem：大屏自适应的双轨方案",
            "ECharts 性能优化：十万级数据点的大屏渲染",
            "从零搭建一个数据可视化平台的前端架构"
    );

    private final ContentRepository repository;
    private final ContentService contentService;
    private final ComicChapterRepository chapterRepository;

    public DataSeeder(
            ContentRepository repository,
            ContentService contentService,
            ComicChapterRepository chapterRepository
    ) {
        this.repository = repository;
        this.contentService = contentService;
        this.chapterRepository = chapterRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedWorks();
        seedComics();
        backfillComicChapters();
        seedMovies();
        seedAnime();
        seedArticles();
        seedHomeConfig();
    }

    private void seedWorks() {
        if (repository.countWorks() > 0) {
            return;
        }
        String base = "/platform/works";
        seedWork(
                "智慧工地总览大屏", base + "/layout-01.png",
                "面向施工现场的实时数据总览，集成人员定位、设备运行与环境监测等多维指标，支持全屏投放与轮播模式。",
                List.of("数据可视化", "大屏"), 2025, "前端负责人", "2025.03 - 2025.06",
                List.of("Vue3", "ECharts", "WebSocket"), base + "/layout-01.png", base + "/layout-04.png"
        );
        seedWork(
                "T梁预制生产线监控", base + "/layout-02.png",
                "对接产线 PLC 与传感器，实时展示工序节拍、质量合格率与产能趋势，异常自动高亮预警。",
                List.of("工业互联网", "ECharts"), 2025, "前端开发", "2025.01 - 2025.04",
                List.of("Vue3", "ECharts", "Modbus"), base + "/layout-02.png", base + "/layout-05.png"
        );
        seedWork(
                "销售实时监控看板", base + "/layout-03.png",
                "多渠道销售数据聚合，支持区域下钻与同比环比对比，帮助管理层快速定位增长点。",
                List.of("BI", "数据大屏"), 2024, "数据可视化设计", "2024.08 - 2024.11",
                List.of("ECharts", "BI", "数据清洗"), base + "/layout-03.png", base + "/layout-06.png"
        );
        seedWork(
                "项目进度驾驶舱", base + "/layout-04.png",
                "以里程碑为主线的进度总览，甘特图与地图联动，直观呈现各标段施工状态与偏差。",
                List.of("项目管理", "GIS"), 2024, "前端负责人", "2024.03 - 2024.07",
                List.of("Vue3", "GIS", "甘特图"), base + "/layout-04.png", base + "/layout-01.png"
        );
        seedWork(
                "环境监测实时面板", base + "/layout-05.png",
                "扬尘、噪声与气象数据联动展示，超标自动推送告警，支持历史回放与趋势预测。",
                List.of("物联网", "监测"), 2024, "前端开发", "2024.01 - 2024.05",
                List.of("Vue3", "物联网", "时间序列"), base + "/layout-05.png", base + "/layout-02.png"
        );
        seedWork(
                "能耗管理分析平台", base + "/layout-06.png",
                "水电燃气多能源分项计量，同比环比与峰谷分析，辅助园区精细化节能运营。",
                List.of("能源", "分析"), 2023, "前端开发", "2023.05 - 2023.10",
                List.of("Vue3", "ECharts", "能源分析"), base + "/layout-06.png", base + "/layout-03.png"
        );
    }

    private void seedWork(
            String title,
            String cover,
            String desc,
            List<String> tags,
            int year,
            String role,
            String period,
            List<String> techStack,
            String firstImage,
            String secondImage
    ) {
        contentService.createWork(new ContentDtos.WorkRequest(
                title,
                cover,
                desc,
                tags,
                year,
                role,
                period,
                null,
                techStack,
                "## 项目背景\n\n" + desc + "\n\n## 我的做法\n\n- 先梳理使用者的决策路径，再决定信息层级。\n- 为核心指标保留实时状态，次要信息放入下层视图。\n- 图表组件统一处理加载、空数据和异常状态。",
                "- 信息获取路径更短，使用者在首屏就能判断当前状态。\n- 长时间投放稳定性更好，异常数据也有明确提示。",
                List.of(firstImage, secondImage)
        ));
    }

    private void seedComics() {
        if (repository.countComics() > 0) {
            return;
        }
        String base = "/platform/comics";
        ContentDtos.ComicDetailResponse cityNight = contentService.createComic(new ContentDtos.ComicRequest(
                "像素星球", base + "/city-night.svg", "连载中", 3, "一个关于像素世界冒险的短篇连载。"
        ));
        seedComicChapters(cityNight, 3, List.of(
                base + "/city-night.svg", base + "/daily-pieces.svg", base + "/short-stories.svg"
        ));
        ContentDtos.ComicDetailResponse dailyPieces = contentService.createComic(new ContentDtos.ComicRequest(
                "深夜代码", base + "/daily-pieces.svg", "完结", 4, "程序员日常吐槽四格漫画合集。"
        ));
        seedComicChapters(dailyPieces, 4, List.of(
                base + "/daily-pieces.svg", base + "/city-night.svg", base + "/short-stories.svg"
        ));
        ContentDtos.ComicDetailResponse shortStories = contentService.createComic(new ContentDtos.ComicRequest(
                "猫与终端", base + "/short-stories.svg", "连载中", 2, "一只猫和一台旧终端的治愈故事。"
        ));
        seedComicChapters(shortStories, 2, List.of(
                base + "/short-stories.svg", base + "/city-night.svg", base + "/daily-pieces.svg"
        ));
    }

    private void seedMovies() {
        if (repository.countMovies() > 0) {
            return;
        }
        String source = "http://vedio.yxdsrq.com";
        contentService.createMovie(movie("现实实验", "https://img.guangsuimage.com/cover/d542679cd869484e83686b45affc5579.jpg", source + "/kongbupian/xianshishiyan/", 2026, "6.0", "恐怖", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("失控撞劫", "https://img.202766.xyz/douban/37812480.webp", source + "/kongbupian/shikongzhuangjie1/", 2026, "5.0", "恐怖", "来源状态：HD。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("凶楼绝路", "https://img.guangsuimage.com/cover/cadf63424798d6a6f53d2a23c14da0f1.jpg", source + "/kongbupian/xiongloujuelu/", 2026, "6.0", "恐怖", "来源状态：抢先版。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("希望囚徒", "https://img.jisuimage.com/cover/af8eb9275a36df48f1da258095f1d4d9.jpg", source + "/jilupian/xiwangqiutu/", 2026, "8.0", "纪录", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("勿信楼上人", "https://img.guangsuimage.com/cover/e531f0b2bddcb79bd0a8a814be181cb6.jpg", source + "/juqingpian/wuxinloushangren/", 2026, "2.0", "剧情", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("心跳声", "https://img.guangsuimage.com/cover/e2afccb9c06008edca76eaefb606d4bd.jpg", source + "/juqingpian/xintiaosheng/", 2026, "8.0", "剧情", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("储物柜", "https://img.guangsuimage.com/cover/408eb7f43ae6263d261b4e66d8b65ecf.jpg", source + "/kongbupian/chuwugui/", 2026, "2.0", "恐怖", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("疯狂穷富翁", "https://img.lzipic.com/upload/vod/20261003-1/6891584826d24e996b55528f0e83a3b6.webp", source + "/juqingpian/fengkuangqiongfuweng/", 2026, "5.0", "剧情", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("普贾，我的爱人", "https://img.guangsuimage.com/cover/6327d4ebbea5d09bcfbab280d9ff5fba.jpg", source + "/kongbupian/pujiawodeairen/", 2026, "3.0", "恐怖", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
        contentService.createMovie(movie("别为往事懊恼", "https://img.guangsuimage.com/cover/ed0a89247c828bbf05a6506f4fa54745.jpg", source + "/jilupian/bieweiwangshiaonao/", 2026, "6.0", "纪录", "来源状态：正片。", null, null, List.of(), null, null, null, null, null));
    }

    private void seedAnime() {
        if (repository.countAnime() > 0) {
            return;
        }
        String source = "http://vedio.yxdsrq.com";
        contentService.createAnime(anime("遮天", "https://img.jisuimage.com/cover/eabe8e1fe6add3ddf6d65b6df954b376.jpg", source + "/dongman/zhetian/", 2026, "10.0", "玄幻", "第15集", 15));
        contentService.createAnime(anime("遮天动画版", "https://img.lzipic.com/upload/vod/20230503-1/5a197f31f09c5c3a41ea8f555e2e2a04.jpg", source + "/dongman/zhetiandonghuaban/", 2026, "10.0", "玄幻", "更新至第15集", 15));
        contentService.createAnime(anime("九阳武神", "https://img.lzipic.com/upload/vod/20251122-1/0e36ecf8bb53e958c8c17e97a4b40833.jpg", source + "/dongman/jiuyangwushen/", 2026, "6.0", "热血", "更新至第03集", 3));
        contentService.createAnime(anime("动态漫画·全民诡异：开局掌握零元购", "https://img.lzipic.com/upload/vod/20250415-1/7ecb36e7b9450ee2185e3b3a8e0f9de8.jpg", source + "/dongman/dongtaimanhuaquanminguiyikaijuzhangwolingyuangou/", 2026, "2.0", "悬疑", "更新至第09集", 9));
        contentService.createAnime(anime("掌门低调点", "https://img.jisuimage.com/cover/d3ed9e3df6007d59a203f7b692880796.jpg", source + "/dongman/zhangmendidiaodian/", 2026, "7.0", "搞笑", "第24集", 24));
        contentService.createAnime(anime("灵武大陆", "https://img.lzipic.com/upload/vod/20240726-1/5d95edc29d2ad7ed71c757cc855bb446.jpg", source + "/dongman/lingwudalu/", 2026, "6.0", "玄幻", "更新至第04集", 4));
        contentService.createAnime(anime("仙逆", "https://img.jisuimage.com/cover/896b690b9566f53a875e03a3a324c091.jpg", source + "/dongman/xianni/", 2026, "10.0", "仙侠", "更新至第03集", 3));
        contentService.createAnime(anime("都市古仙医", "https://img.lzipic.com/upload/vod/20241002-1/28ccdda6571d92bf5ecad9a666647e99.jpg", source + "/dongman/dushiguxianyi/", 2026, "3.0", "都市", "更新至第06集", 6));
        contentService.createAnime(anime("牧神记", "https://img.lzipic.com/upload/vod/20241027-1/f6c217351f2b1ef044263484af7f7dd4.jpg", source + "/dongman/mushenji/", 2026, "7.0", "玄幻", "更新至第02集", 2));
        contentService.createAnime(anime("师尊：这个冲师逆徒才不是圣子 动态漫画", "https://img.guangsuimage.com/cover/ef787dffe53627045b60ebd58188fa30.jpg", source + "/dongman/shizunzhegechongshinitucaibushishengzidongtaimanhua/", 2026, "7.0", "恋爱", "第1集", 1));
    }

    private ContentDtos.MovieRequest movie(
            String title,
            String cover,
            String playUrl,
            int year,
            String rating,
            String genre,
            String note,
            String watchDate,
            String director,
            List<String> castNames,
            Integer storyScore,
            Integer visualScore,
            Integer soundScore,
            Integer rewatchValue,
            String review
    ) {
        return new ContentDtos.MovieRequest(
                title, cover, playUrl, year, new BigDecimal(rating), genre, note, watchDate,
                director, castNames, review, storyScore, visualScore, soundScore, rewatchValue
        );
    }

    private ContentDtos.AnimeRequest anime(
            String title,
            String cover,
            String playUrl,
            int year,
            String rating,
            String genre,
            String status,
            int episodes
    ) {
        return new ContentDtos.AnimeRequest(
                title,
                cover,
                playUrl,
                year,
                new BigDecimal(rating),
                genre,
                status,
                episodes,
                "数据来源：星空影院动漫栏目。",
                null
        );
    }

    private void seedComicChapters(
            ContentDtos.ComicDetailResponse comic,
            int count,
            List<String> pages
    ) {
        for (int index = 1; index <= count; index += 1) {
            contentService.createComicChapter(comic.id(), new ContentDtos.ComicChapterRequest(
                    index,
                    "第 %d 话".formatted(index),
                    "像素、终端与日常观察的第 %d 段小记录。".formatted(index),
                    pages,
                    "2025-%02d-%02d".formatted(index, index + 1)
            ));
        }
    }

    private void backfillComicChapters() {
        var comics = contentService.listComics(null, null, 1, 10000).items();
        for (ContentDtos.ComicResponse comic : comics) {
            if (!chapterRepository.list(comic.id()).isEmpty() || comic.chapters() <= 0) {
                continue;
            }
            List<String> pages = comic.cover() == null
                    ? List.of()
                    : List.of(comic.cover(), comic.cover(), comic.cover());
            for (int index = 1; index <= comic.chapters(); index += 1) {
                contentService.createComicChapter(comic.id(), new ContentDtos.ComicChapterRequest(
                        index,
                        "第 %d 话".formatted(index),
                        "本地存量章节，正文和页面可在后台上传补充。",
                        pages,
                        "2025-01-%02d".formatted(Math.min(28, index))
                ));
            }
        }
    }

    private void seedArticles() {
        removeLegacyArticles();
        readJuejinArticles().forEach(this::seedJuejinArticle);
    }

    private void removeLegacyArticles() {
        Map<String, String> articlesByTitle = contentService.listAllArticles().stream()
                .collect(Collectors.toMap(
                        ContentDtos.ArticleSummaryResponse::title,
                        ContentDtos.ArticleSummaryResponse::id,
                        (left, right) -> left
                ));
        LEGACY_ARTICLE_TITLES.stream()
                .map(articlesByTitle::get)
                .filter(id -> id != null)
                .forEach(repository::deleteArticle);
    }

    private JsonNode readJuejinArticles() {
        try {
            return OBJECT_MAPPER.readTree(
                    new ClassPathResource("data/juejin-articles.json").getInputStream()
            );
        } catch (IOException exception) {
            throw new UncheckedIOException("无法读取掘金文章种子数据", exception);
        }
    }

    private void seedJuejinArticle(JsonNode item) {
        String id = "juejin-" + item.path("id").asText();
        String title = item.path("title").asText();
        String category = item.path("category").asText();
        Set<String> tags = new LinkedHashSet<>();
        item.path("tags").forEach(tag -> tags.add(tag.asText()));
        String tagNames = String.join(" / ", tags);
        tags.add(category);
        String desc = "掘金%s文章，主题：%s。".formatted(category, tagNames);
        String content = item.path("content").asText("");
        if (content.isBlank()) {
            content = """
                    ## 原文

                    本文发布于掘金，站内暂未导入完整正文。

                    [阅读原文](https://juejin.cn/post/%s)
                    """.formatted(item.path("id").asText());
        }

        var existing = repository.findArticle(id);
        if (existing.isPresent()) {
            String existingContent = existing.get().content();
            boolean needsUpgrade = existingContent == null || existingContent.isBlank()
                    || existingContent.contains("站内暂未导入完整正文")
                    || existingContent.startsWith(".markdown-body{");
            if (!content.isBlank() && needsUpgrade) {
                repository.updateArticle(
                        id,
                        title,
                        desc,
                        String.join(",", tags),
                        item.path("date").asText(),
                        content,
                        "published",
                        null
                );
            }
            return;
        }

        repository.insertArticle(
                id,
                title,
                desc,
                String.join(",", tags),
                item.path("date").asText(),
                content,
                "published",
                null,
                repository.nextArticleSortNo()
        );
    }

    private void seedHomeConfig() {
        contentService.seedDefaultHomeConfig();
    }
}
