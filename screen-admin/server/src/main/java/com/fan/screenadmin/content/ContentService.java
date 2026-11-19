package com.fan.screenadmin.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContentService {

    private static final String ARTICLE_STATUS_PUBLISHED = "published";
    private static final String ARTICLE_STATUS_DRAFT = "draft";
    private static final int ADMIN_MAX_ITEMS = 10000;

    private final ContentRepository repository;
    private final ComicChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    public ContentService(
            ContentRepository repository,
            ComicChapterRepository chapterRepository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.chapterRepository = chapterRepository;
        this.objectMapper = objectMapper;
    }

    public ContentDtos.PageResponse<ContentDtos.WorkResponse> listWorks(
            String keyword,
            String tag,
            Integer year,
            Integer page,
            Integer pageSize
    ) {
        if (year != null) {
            requireYear(year);
        }
        PageSelection selection = normalizePage(page, pageSize);
        ContentRepository.WorkPage result = repository.pageWorks(new ContentRepository.WorkQuery(
                normalizeKeyword(keyword),
                normalizeKeyword(tag),
                year,
                selection.limit(),
                selection.offset()
        ));
        List<ContentDtos.WorkResponse> items = result.rows().stream()
                .map(row -> new ContentDtos.WorkResponse(
                        row.id(), row.title(), row.cover(), row.desc(), splitTags(row.tags()), row.year()
                ))
                .toList();
        return toPage(items, result.total(), selection);
    }

    public List<ContentDtos.WorkDetailResponse> listAllWorks() {
        ContentRepository.WorkPage result = repository.pageWorks(new ContentRepository.WorkQuery(
                null,
                null,
                null,
                ADMIN_MAX_ITEMS,
                0
        ));
        return result.rows().stream().map(this::mapWorkDetail).toList();
    }

    public ContentDtos.WorkDetailResponse createWork(ContentDtos.WorkRequest request) {
        validateWork(request);
        String id = UUID.randomUUID().toString();
        repository.insertWork(normalizeWorkInput(request), id, repository.nextWorkSortNo());
        return getWork(id);
    }

    public ContentDtos.WorkDetailResponse updateWork(String id, ContentDtos.WorkRequest request) {
        ensureWork(id);
        validateWork(request);
        repository.updateWork(id, normalizeWorkInput(request));
        return getWork(id);
    }

    public void deleteWork(String id) {
        ensureWork(id);
        repository.deleteWork(id);
    }

    public Optional<ContentDtos.WorkDetailResponse> findWork(String id, String visitorKey) {
        ContentRepository.ContentViewStats stats = repository.recordContentView("works", id, visitorKey);
        return repository.findWork(id)
                .map(row -> mapWorkDetail(row, stats));
    }

    public ContentDtos.PageResponse<ContentDtos.ComicResponse> listComics(
            String keyword,
            String status,
            Integer page,
            Integer pageSize
    ) {
        PageSelection selection = normalizePage(page, pageSize);
        ContentRepository.ComicPage result = repository.pageComics(new ContentRepository.ComicQuery(
                normalizeKeyword(keyword),
                normalizeKeyword(status),
                selection.limit(),
                selection.offset()
        ));
        List<ContentDtos.ComicResponse> items = result.rows().stream()
                .map(row -> new ContentDtos.ComicResponse(
                        row.id(), row.title(), row.cover(), row.status(), row.chapters(), row.desc()
                ))
                .toList();
        return toPage(items, result.total(), selection);
    }

    public List<ContentDtos.ComicDetailResponse> listAllComics() {
        ContentRepository.ComicPage result = repository.pageComics(new ContentRepository.ComicQuery(
                null,
                null,
                ADMIN_MAX_ITEMS,
                0
        ));
        return result.rows().stream().map(this::mapComicDetail).toList();
    }

    public ContentDtos.ComicDetailResponse createComic(ContentDtos.ComicRequest request) {
        validateComic(request);
        String id = UUID.randomUUID().toString();
        repository.insertComic(
                id,
                cleanTitle(request.title()),
                cleanUrl(request.cover()),
                cleanText(request.status(), 30),
                requireCount(request.chapters(), "章节数"),
                cleanText(request.desc(), 2000),
                repository.nextComicSortNo()
        );
        return getComic(id);
    }

    public ContentDtos.ComicDetailResponse updateComic(String id, ContentDtos.ComicRequest request) {
        ensureComic(id);
        validateComic(request);
        repository.updateComic(
                id,
                cleanTitle(request.title()),
                cleanUrl(request.cover()),
                cleanText(request.status(), 30),
                requireCount(request.chapters(), "章节数"),
                cleanText(request.desc(), 2000)
        );
        return getComic(id);
    }

    public void deleteComic(String id) {
        ensureComic(id);
        repository.deleteComic(id);
    }

    public Optional<ContentDtos.ComicDetailResponse> findComic(String id, String visitorKey) {
        ContentRepository.ContentViewStats stats = repository.recordContentView("comics", id, visitorKey);
        return repository.findComic(id)
                .map(row -> mapComicDetail(row, stats));
    }

    public ContentDtos.ComicChapterResponse createComicChapter(String comicId, ContentDtos.ComicChapterRequest request) {
        ensureComic(comicId);
        validateComicChapter(request);
        int chapterNo = request.chapterNo() == null
                ? chapterRepository.nextChapterNo(comicId)
                : request.chapterNo();
        String id = UUID.randomUUID().toString();
        chapterRepository.insert(
                id,
                comicId,
                chapterNo,
                cleanTitle(request.title()),
                cleanText(request.summary(), 1000),
                joinLines(normalizeImageUrls(request.pages())),
                optionalDate(request.date())
        );
        chapterRepository.syncComicChapterCount(comicId);
        return getComicChapter(id);
    }

    public ContentDtos.ComicChapterResponse updateComicChapter(
            String id,
            ContentDtos.ComicChapterRequest request
    ) {
        ComicChapterRepository.ComicChapterRow existing = chapterRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
        validateComicChapter(request);
        int chapterNo = request.chapterNo() == null ? existing.chapterNo() : request.chapterNo();
        chapterRepository.update(
                id,
                chapterNo,
                cleanTitle(request.title()),
                cleanText(request.summary(), 1000),
                joinLines(normalizeImageUrls(request.pages())),
                optionalDate(request.date())
        );
        chapterRepository.syncComicChapterCount(existing.comicId());
        return getComicChapter(id);
    }

    public void deleteComicChapter(String id) {
        ComicChapterRepository.ComicChapterRow existing = chapterRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
        chapterRepository.delete(id);
        chapterRepository.syncComicChapterCount(existing.comicId());
    }

    public ContentDtos.PageResponse<ContentDtos.MovieResponse> listMovies(
            String keyword,
            String genre,
            Integer year,
            Integer page,
            Integer pageSize
    ) {
        if (year != null) {
            requireYear(year);
        }
        PageSelection selection = normalizePage(page, pageSize);
        ContentRepository.MoviePage result = repository.pageMovies(new ContentRepository.MovieQuery(
                normalizeKeyword(keyword),
                normalizeKeyword(genre),
                year,
                selection.limit(),
                selection.offset()
        ));
        List<ContentDtos.MovieResponse> items = result.rows().stream().map(this::mapMovie).toList();
        return toPage(items, result.total(), selection);
    }

    public List<ContentDtos.MovieResponse> listAllMovies() {
        ContentRepository.MoviePage result = repository.pageMovies(new ContentRepository.MovieQuery(
                null,
                null,
                null,
                ADMIN_MAX_ITEMS,
                0
        ));
        return result.rows().stream().map(this::mapMovie).toList();
    }

    public ContentDtos.MovieResponse createMovie(ContentDtos.MovieRequest request) {
        validateMovie(request);
        String id = UUID.randomUUID().toString();
        repository.insertMovie(normalizeMovieInput(request), id, repository.nextMovieSortNo());
        return getMovie(id);
    }

    public ContentDtos.MovieResponse updateMovie(String id, ContentDtos.MovieRequest request) {
        ensureMovie(id);
        validateMovie(request);
        repository.updateMovie(id, normalizeMovieInput(request));
        return getMovie(id);
    }

    public void deleteMovie(String id) {
        ensureMovie(id);
        repository.deleteMovie(id);
    }

    public Optional<ContentDtos.MovieDetailResponse> findMovie(String id, String visitorKey) {
        ContentRepository.ContentViewStats stats = repository.recordContentView("movies", id, visitorKey);
        return repository.findMovie(id)
                .map(row -> mapMovieDetail(row, stats));
    }

    public ContentDtos.PageResponse<ContentDtos.AnimeResponse> listAnime(
            String keyword,
            String genre,
            Integer year,
            Integer page,
            Integer pageSize
    ) {
        if (year != null) {
            requireYear(year);
        }
        PageSelection selection = normalizePage(page, pageSize);
        ContentRepository.AnimePage result = repository.pageAnime(new ContentRepository.AnimeQuery(
                normalizeKeyword(keyword),
                normalizeKeyword(genre),
                year,
                selection.limit(),
                selection.offset()
        ));
        List<ContentDtos.AnimeResponse> items = result.rows().stream().map(this::mapAnime).toList();
        return toPage(items, result.total(), selection);
    }

    public List<ContentDtos.AnimeResponse> listAllAnime() {
        ContentRepository.AnimePage result = repository.pageAnime(new ContentRepository.AnimeQuery(
                null,
                null,
                null,
                ADMIN_MAX_ITEMS,
                0
        ));
        return result.rows().stream().map(this::mapAnime).toList();
    }

    public ContentDtos.AnimeResponse createAnime(ContentDtos.AnimeRequest request) {
        validateAnime(request);
        String id = UUID.randomUUID().toString();
        repository.insertAnime(normalizeAnimeInput(request), id, repository.nextAnimeSortNo());
        return getAnime(id);
    }

    public ContentDtos.AnimeResponse updateAnime(String id, ContentDtos.AnimeRequest request) {
        ensureAnime(id);
        validateAnime(request);
        repository.updateAnime(id, normalizeAnimeInput(request));
        return getAnime(id);
    }

    public void deleteAnime(String id) {
        ensureAnime(id);
        repository.deleteAnime(id);
    }

    public Optional<ContentDtos.AnimeDetailResponse> findAnime(String id, String visitorKey) {
        ContentRepository.ContentViewStats stats = repository.recordContentView("anime", id, visitorKey);
        return repository.findAnime(id)
                .map(row -> mapAnimeDetail(row, stats));
    }

    public ContentDtos.PageResponse<ContentDtos.ArticleSummaryResponse> listArticles(
            String keyword,
            String tag,
            Integer year,
            Integer page,
            Integer pageSize
    ) {
        PageSelection selection = normalizePage(page, pageSize);
        ContentRepository.ArticlePage result = repository.pageArticles(articleQuery(keyword, tag, year, selection), true);
        List<ContentDtos.ArticleSummaryResponse> items = result.rows().stream()
                .map(row -> new ContentDtos.ArticleSummaryResponse(
                        row.id(), row.title(), row.desc(), splitTags(row.tags()), row.date(), row.status(), row.scheduledAt(), row.viewCount()
                ))
                .toList();
        return toPage(items, result.total(), selection);
    }

    public List<ContentDtos.ArticleSummaryResponse> listAllArticles() {
        PageSelection selection = normalizePage(1, 10000);
        return repository.pageArticles(articleQuery(null, null, null, selection), false).rows().stream()
                .map(row -> new ContentDtos.ArticleSummaryResponse(
                        row.id(), row.title(), row.desc(), splitTags(row.tags()), row.date(), row.status(), row.scheduledAt(), row.viewCount()
                ))
                .toList();
    }

    public List<ContentDtos.ArticleDetailResponse> listAllArticleDetails() {
        PageSelection selection = normalizePage(1, 10000);
        return repository.pageArticles(articleQuery(null, null, null, selection), false).rows().stream()
                .map(this::mapArticleAdminDetail)
                .toList();
    }

    public ContentDtos.ContentFacetResponse listFacets() {
        return new ContentDtos.ContentFacetResponse(
                distinctTags(repository.listWorkTags()),
                repository.listWorkYears(),
                repository.listComicStatuses(),
                repository.listMovieGenres(),
                repository.listMovieYears(),
                repository.listAnimeGenres(),
                repository.listAnimeYears(),
                distinctTags(repository.listArticleTags()),
                repository.listArticleYears()
        );
    }

    public ContentDtos.GlobalSearchResponse search(String keyword, Integer limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return new ContentDtos.GlobalSearchResponse(List.of(), List.of(), List.of(), List.of(), List.of(), 0);
        }

        int searchLimit = Math.min(Math.max(limit == null || limit < 1 ? 5 : limit, 1), 20);
        var works = listWorks(normalizedKeyword, null, null, 1, searchLimit);
        var comics = listComics(normalizedKeyword, null, 1, searchLimit);
        var movies = listMovies(normalizedKeyword, null, null, 1, searchLimit);
        var anime = listAnime(normalizedKeyword, null, null, 1, searchLimit);
        var articles = listArticles(normalizedKeyword, null, null, 1, searchLimit);

        return new ContentDtos.GlobalSearchResponse(
                works.items(),
                comics.items(),
                movies.items(),
                anime.items(),
                articles.items(),
                works.total() + comics.total() + movies.total() + anime.total() + articles.total()
        );
    }

    public ContentDtos.DashboardResponse getDashboard() {
        List<ContentDtos.ArticleSummaryResponse> popularArticles = repository.listPopularArticles(5).stream()
                .map(this::mapArticleSummary)
                .toList();
        List<ContentDtos.ContactMessageResponse> recentMessages = repository.listContactMessages().stream()
                .limit(5)
                .map(this::mapContactMessage)
                .toList();

        return new ContentDtos.DashboardResponse(
                repository.countWorks(),
                repository.countComics(),
                repository.countMovies(),
                repository.countAnime(),
                repository.countArticles(),
                repository.countPublishedArticles(),
                repository.countDraftArticles(),
                repository.sumArticleViews(),
                repository.countUnreadMessages(),
                popularArticles,
                recentMessages
        );
    }

    public ContentDtos.ContactMessageResponse createContactMessage(ContentDtos.ContactMessageRequest request) {
        String name = requireText(request.name(), "称呼", 50);
        String contact = requireText(request.contact(), "联系方式", 100);
        String content = requireText(request.content(), "留言内容", 1000);
        String id = UUID.randomUUID().toString();
        repository.insertContactMessage(id, name, contact, content);
        return getContactMessage(id);
    }

    public List<ContentDtos.ContactMessageResponse> listContactMessages() {
        return repository.listContactMessages().stream()
                .map(this::mapContactMessage)
                .toList();
    }

    public ContentDtos.ContactMessageResponse markContactMessageHandled(String id) {
        if (repository.updateContactMessageHandled(id, true) == 0) {
            throw new IllegalArgumentException("留言不存在");
        }
        return getContactMessage(id);
    }

    public void deleteContactMessage(String id) {
        if (repository.deleteContactMessage(id) == 0) {
            throw new IllegalArgumentException("留言不存在");
        }
    }

    public List<ContentDtos.CommentResponse> listApprovedComments(String contentType, String contentId) {
        requireContentType(contentType);
        return repository.listApprovedComments(contentType, contentId).stream()
                .map(ContentService::mapComment)
                .toList();
    }

    public List<ContentDtos.CommentResponse> listAllComments() {
        return repository.listAllComments().stream()
                .map(ContentService::mapComment)
                .toList();
    }

    public ContentDtos.CommentResponse createComment(
            String contentType,
            String contentId,
            String visitorKey,
            ContentDtos.CommentRequest request
    ) {
        requireContentType(contentType);
        String authorName = requireText(request.authorName(), "昵称", 50);
        String body = requireText(request.body(), "评论内容", 1000);
        String id = UUID.randomUUID().toString();
        repository.insertComment(id, contentType, contentId, visitorKey, authorName, body, "approved");
        return repository.listApprovedComments(contentType, contentId).stream()
                .filter(row -> row.id().equals(id))
                .map(ContentService::mapComment)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("评论创建失败"));
    }

    public ContentDtos.CommentResponse updateCommentStatus(String id, String status) {
        String normalized = normalizeCommentStatus(status);
        if (repository.updateCommentStatus(id, normalized) == 0) {
            throw new IllegalArgumentException("评论不存在");
        }
        return repository.listAllComments().stream()
                .filter(row -> row.id().equals(id))
                .map(ContentService::mapComment)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
    }

    public void deleteComment(String id) {
        if (repository.deleteComment(id) == 0) {
            throw new IllegalArgumentException("评论不存在");
        }
    }

    public Optional<ContentDtos.ReadingProgressResponse> getReadingProgress(String visitorKey, String comicId) {
        return repository.findReadingProgress(visitorKey, comicId)
                .map(row -> new ContentDtos.ReadingProgressResponse(
                        row.comicId(),
                        row.chapterId(),
                        row.chapterNo(),
                        row.updatedAt()
                ));
    }

    public ContentDtos.ReadingProgressResponse saveReadingProgress(
            String visitorKey,
            String comicId,
            ContentDtos.ReadingProgressRequest request
    ) {
        if (visitorKey == null || visitorKey.isBlank()) {
            throw new IllegalArgumentException("访客标识不能为空");
        }
        if (request.chapterId() == null || request.chapterId().isBlank()) {
            throw new IllegalArgumentException("章节标识不能为空");
        }
        if (request.chapterNo() == null || request.chapterNo() < 1) {
            throw new IllegalArgumentException("章节序号必须大于0");
        }
        repository.upsertReadingProgress(visitorKey, comicId, request.chapterId(), request.chapterNo());
        return new ContentDtos.ReadingProgressResponse(
                comicId,
                request.chapterId(),
                request.chapterNo(),
                java.time.LocalDateTime.now().toString()
        );
    }

    public String generateRssFeed() {
        PageSelection selection = normalizePage(1, 20);
        List<ContentRepository.ArticleRow> articles = repository.pageArticles(
                new ContentRepository.ArticleQuery(null, null, null, selection.limit(), selection.offset()),
                true
        ).rows();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("<channel>\n");
        xml.append("<title>FAN · 凡集 · 技术分享</title>\n");
        xml.append("<link>http://localhost:8666</link>\n");
        xml.append("<description>技术分享与项目作品</description>\n");
        xml.append("<language>zh-cn</language>\n");
        for (ContentRepository.ArticleRow article : articles) {
            String safeTitle = article.title()
                    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            String safeDesc = article.desc() == null ? "" : article.desc()
                    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            xml.append("<item>\n");
            xml.append("<title>").append(safeTitle).append("</title>\n");
            xml.append("<link>http://localhost:8666/#/platform/articles/").append(article.id()).append("</link>\n");
            xml.append("<description>").append(safeDesc).append("</description>\n");
            xml.append("<pubDate>").append(article.date()).append("</pubDate>\n");
            xml.append("<guid>/platform/articles/").append(article.id()).append("</guid>\n");
            xml.append("</item>\n");
        }
        xml.append("</channel>\n");
        xml.append("</rss>\n");
        return xml.toString();
    }

    private void requireContentType(String contentType) {
        if (!List.of("works", "comics", "movies", "anime", "articles").contains(contentType)) {
            throw new IllegalArgumentException("不支持的内容类型");
        }
    }

    private String normalizeCommentStatus(String status) {
        if ("approved".equals(status) || "hidden".equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("评论状态必须是approved或hidden");
    }

    private static ContentDtos.CommentResponse mapComment(ContentRepository.CommentRow row) {
        return new ContentDtos.CommentResponse(
                row.id(),
                row.contentType(),
                row.contentId(),
                row.authorName(),
                row.body(),
                row.status(),
                row.createdAt()
        );
    }

    public ContentDtos.ArticleSummaryResponse createArticle(ContentDtos.ArticleRequest request) {
        validateArticle(request);
        String id = UUID.randomUUID().toString();
        repository.insertArticle(
                id,
                cleanTitle(request.title()),
                cleanText(request.desc(), 2000),
                joinTags(request.tags()),
                requireDate(request.date()),
                cleanText(request.content(), 100000),
                normalizeArticleStatus(request.status()),
                request.scheduledAt(),
                repository.nextArticleSortNo()
        );
        return getArticle(id);
    }

    public ContentDtos.ArticleSummaryResponse updateArticle(String id, ContentDtos.ArticleRequest request) {
        ensureArticle(id);
        validateArticle(request);
        repository.updateArticle(
                id,
                cleanTitle(request.title()),
                cleanText(request.desc(), 2000),
                joinTags(request.tags()),
                requireDate(request.date()),
                cleanText(request.content(), 100000),
                normalizeArticleStatus(request.status()),
                request.scheduledAt()
        );
        return getArticle(id);
    }

    public void deleteArticle(String id) {
        ensureArticle(id);
        repository.deleteArticle(id);
    }

    public Optional<ContentDtos.ArticleDetailResponse> findPublishedArticle(String id, String visitorKey) {
        boolean published = repository.findArticle(id)
                .filter(row -> ARTICLE_STATUS_PUBLISHED.equals(row.status()))
                .isPresent();
        if (!published) {
            return Optional.empty();
        }

        ContentRepository.ContentViewStats stats = repository.recordContentView("articles", id, visitorKey);
        return repository.findArticle(id)
                .filter(row -> ARTICLE_STATUS_PUBLISHED.equals(row.status()))
                .map(row -> mapArticleDetail(row, stats));
    }

    public ContentDtos.HomeConfigResponse getHomeConfig() {
        return repository.findHomeConfig()
                .map(this::parseHomeConfig)
                .orElseGet(() -> defaultHomeConfig());
    }

    public ContentDtos.HomeConfigResponse saveHomeConfig(ContentDtos.HomeConfigRequest request) {
        ContentDtos.HomeConfigResponse response = normalizeHomeConfig(request);
        repository.saveHomeConfig(writeHomeConfig(response));
        return getHomeConfig();
    }

    public void seedDefaultHomeConfig() {
        if (repository.findHomeConfig().isEmpty()) {
            repository.saveHomeConfig(writeHomeConfig(defaultHomeConfig()));
        }
    }

    public ContentDtos.WorkDetailResponse getWork(String id) {
        return repository.findWork(id)
                .map(this::mapWorkDetail)
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));
    }

    private ContentDtos.ComicDetailResponse getComic(String id) {
        return repository.findComic(id)
                .map(this::mapComicDetail)
                .orElseThrow(() -> new IllegalArgumentException("漫画不存在"));
    }

    private ContentDtos.ComicChapterResponse getComicChapter(String id) {
        return chapterRepository.find(id)
                .map(this::mapComicChapter)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
    }

    private ContentDtos.MovieResponse getMovie(String id) {
        return repository.findMovie(id)
                .map(this::mapMovie)
                .orElseThrow(() -> new IllegalArgumentException("电影不存在"));
    }

    private ContentDtos.AnimeResponse getAnime(String id) {
        return repository.findAnime(id)
                .map(this::mapAnime)
                .orElseThrow(() -> new IllegalArgumentException("动漫不存在"));
    }

    private ContentDtos.ArticleSummaryResponse getArticle(String id) {
        return repository.findArticle(id)
                .map(row -> new ContentDtos.ArticleSummaryResponse(
                        row.id(), row.title(), row.desc(), splitTags(row.tags()), row.date(), row.status(), row.scheduledAt(), row.viewCount()
                ))
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
    }

    private ContentDtos.ContactMessageResponse getContactMessage(String id) {
        return repository.listContactMessages().stream()
                .filter(row -> row.id().equals(id))
                .map(this::mapContactMessage)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("留言不存在"));
    }

    private ContentDtos.ContactMessageResponse mapContactMessage(ContentRepository.ContactMessageRow row) {
        return new ContentDtos.ContactMessageResponse(
                row.id(),
                row.name(),
                row.contact(),
                row.content(),
                row.handled(),
                row.createdAt()
        );
    }

    private ContentDtos.WorkDetailResponse mapWorkDetail(ContentRepository.WorkRow row) {
        return mapWorkDetail(row, new ContentRepository.ContentViewStats(0, 0));
    }

    private ContentDtos.WorkDetailResponse mapWorkDetail(
            ContentRepository.WorkRow row,
            ContentRepository.ContentViewStats stats
    ) {
        return new ContentDtos.WorkDetailResponse(
                row.id(),
                row.title(),
                row.cover(),
                row.desc(),
                splitTags(row.tags()),
                row.year(),
                row.role(),
                row.period(),
                row.projectUrl(),
                splitTags(row.techStack()),
                row.content(),
                row.result(),
                splitLines(row.images()),
                stats.viewCount(),
                stats.visitorCount(),
                relatedWorks(row)
        );
    }

    private List<ContentDtos.RelatedContentResponse> relatedWorks(ContentRepository.WorkRow current) {
        List<String> tags = splitTags(current.tags());
        return repository.pageWorks(new ContentRepository.WorkQuery(
                null, null, null, ADMIN_MAX_ITEMS, 0
        )).rows().stream()
                .filter(item -> !item.id().equals(current.id()))
                .filter(item -> {
                    List<String> itemTags = splitTags(item.tags());
                    return itemTags.stream().anyMatch(tags::contains);
                })
                .sorted((left, right) -> Long.compare(
                        splitTags(right.tags()).stream().filter(tags::contains).count(),
                        splitTags(left.tags()).stream().filter(tags::contains).count()
                ))
                .limit(3)
                .map(item -> new ContentDtos.RelatedContentResponse(
                        item.id(), item.title(), item.cover(), "works"
                ))
                .toList();
    }

    private ContentDtos.ComicResponse mapComic(ContentRepository.ComicRow row) {
        return new ContentDtos.ComicResponse(
                row.id(), row.title(), row.cover(), row.status(), row.chapters(), row.desc()
        );
    }

    private ContentDtos.ComicDetailResponse mapComicDetail(ContentRepository.ComicRow row) {
        return mapComicDetail(row, new ContentRepository.ContentViewStats(0, 0));
    }

    private ContentDtos.ComicDetailResponse mapComicDetail(
            ContentRepository.ComicRow row,
            ContentRepository.ContentViewStats stats
    ) {
        return new ContentDtos.ComicDetailResponse(
                row.id(),
                row.title(),
                row.cover(),
                row.status(),
                row.chapters(),
                row.desc(),
                chapterRepository.list(row.id()).stream().map(this::mapComicChapter).toList(),
                stats.viewCount(),
                stats.visitorCount(),
                relatedComics(row)
        );
    }

    private List<ContentDtos.RelatedContentResponse> relatedComics(ContentRepository.ComicRow current) {
        return repository.pageComics(new ContentRepository.ComicQuery(
                null, null, ADMIN_MAX_ITEMS, 0
        )).rows().stream()
                .filter(item -> !item.id().equals(current.id()))
                .limit(3)
                .map(item -> new ContentDtos.RelatedContentResponse(
                        item.id(), item.title(), item.cover(), "comics"
                ))
                .toList();
    }

    private ContentDtos.ComicChapterResponse mapComicChapter(ComicChapterRepository.ComicChapterRow row) {
        return new ContentDtos.ComicChapterResponse(
                row.id(),
                row.comicId(),
                row.chapterNo(),
                row.title(),
                row.summary(),
                splitLines(row.pages()),
                row.date()
        );
    }

    private ContentDtos.MovieResponse mapMovie(ContentRepository.MovieRow row) {
        return new ContentDtos.MovieResponse(
                row.id(),
                row.title(),
                row.cover(),
                row.playUrl(),
                row.year(),
                row.rating(),
                row.genre(),
                row.note(),
                row.watchDate(),
                row.director(),
                splitTags(row.castNames()),
                row.review(),
                row.storyScore(),
                row.visualScore(),
                row.soundScore(),
                row.rewatchValue()
        );
    }

    private ContentDtos.MovieDetailResponse mapMovieDetail(
            ContentRepository.MovieRow row,
            ContentRepository.ContentViewStats stats
    ) {
        return mapMovieDetailValues(row, stats);
    }

    private ContentDtos.MovieDetailResponse mapMovieDetailValues(
            ContentRepository.MovieRow row,
            ContentRepository.ContentViewStats stats
    ) {
        return new ContentDtos.MovieDetailResponse(
                row.id(),
                row.title(),
                row.cover(),
                row.playUrl(),
                row.year(),
                row.rating(),
                row.genre(),
                row.note(),
                row.watchDate(),
                row.director(),
                splitTags(row.castNames()),
                row.review(),
                row.storyScore(),
                row.visualScore(),
                row.soundScore(),
                row.rewatchValue(),
                stats.viewCount(),
                stats.visitorCount(),
                relatedMovies(row)
        );
    }

    private List<ContentDtos.RelatedContentResponse> relatedMovies(ContentRepository.MovieRow current) {
        return repository.pageMovies(new ContentRepository.MovieQuery(
                null, current.genre(), null, ADMIN_MAX_ITEMS, 0
        )).rows().stream()
                .filter(item -> !item.id().equals(current.id()))
                .limit(3)
                .map(item -> new ContentDtos.RelatedContentResponse(
                        item.id(), item.title(), item.cover(), "movies"
                ))
                .toList();
    }

    private ContentDtos.AnimeResponse mapAnime(ContentRepository.AnimeRow row) {
        return new ContentDtos.AnimeResponse(
                row.id(),
                row.title(),
                row.cover(),
                row.playUrl(),
                row.year(),
                row.rating(),
                row.genre(),
                row.status(),
                row.episodes(),
                row.note(),
                row.review()
        );
    }

    private ContentDtos.AnimeDetailResponse mapAnimeDetail(
            ContentRepository.AnimeRow row,
            ContentRepository.ContentViewStats stats
    ) {
        return new ContentDtos.AnimeDetailResponse(
                row.id(),
                row.title(),
                row.cover(),
                row.playUrl(),
                row.year(),
                row.rating(),
                row.genre(),
                row.status(),
                row.episodes(),
                row.note(),
                row.review(),
                stats.viewCount(),
                stats.visitorCount(),
                relatedAnime(row)
        );
    }

    private List<ContentDtos.RelatedContentResponse> relatedAnime(ContentRepository.AnimeRow current) {
        return repository.pageAnime(new ContentRepository.AnimeQuery(
                null, current.genre(), null, ADMIN_MAX_ITEMS, 0
        )).rows().stream()
                .filter(item -> !item.id().equals(current.id()))
                .limit(3)
                .map(item -> new ContentDtos.RelatedContentResponse(
                        item.id(), item.title(), item.cover(), "anime"
                ))
                .toList();
    }

    private ContentRepository.MovieInput normalizeMovieInput(ContentDtos.MovieRequest request) {
        return new ContentRepository.MovieInput(
                cleanTitle(request.title()),
                cleanUrl(request.cover()),
                cleanUrl(request.playUrl()),
                requireYear(request.year()),
                requireRating(request.rating()),
                cleanText(request.genre(), 50),
                cleanText(request.note(), 1000),
                optionalDate(request.watchDate()),
                cleanText(request.director(), 150),
                joinTags(request.castNames()),
                cleanText(request.review(), 100000),
                requireScore(request.storyScore(), "故事评分"),
                requireScore(request.visualScore(), "视觉评分"),
                requireScore(request.soundScore(), "声音评分"),
                requireScore(request.rewatchValue(), "重看指数")
        );
    }

    private ContentRepository.AnimeInput normalizeAnimeInput(ContentDtos.AnimeRequest request) {
        return new ContentRepository.AnimeInput(
                cleanTitle(request.title()),
                cleanUrl(request.cover()),
                cleanUrl(request.playUrl()),
                requireYear(request.year()),
                requireRating(request.rating()),
                cleanText(request.genre(), 50),
                cleanText(request.status(), 50),
                requireCount(request.episodes(), "集数"),
                cleanText(request.note(), 1000),
                cleanText(request.review(), 100000)
        );
    }

    private ContentDtos.ArticleDetailResponse mapArticleDetail(ContentRepository.ArticleRow row) {
        return mapArticleDetail(row, new ContentRepository.ContentViewStats(row.viewCount(), 0));
    }

    private ContentDtos.ArticleDetailResponse mapArticleAdminDetail(ContentRepository.ArticleRow row) {
        return new ContentDtos.ArticleDetailResponse(
                row.id(),
                row.title(),
                row.desc(),
                splitTags(row.tags()),
                row.date(),
                row.status(),
                cleanText(row.content(), 100000),
                row.viewCount(),
                0,
                null,
                null,
                List.of()
        );
    }

    private ContentDtos.ArticleDetailResponse mapArticleDetail(
            ContentRepository.ArticleRow row,
            ContentRepository.ContentViewStats stats
    ) {
        List<ContentRepository.ArticleRow> publishedRows = repository.pageArticles(
                new ContentRepository.ArticleQuery(null, null, null, ADMIN_MAX_ITEMS, 0),
                true
        ).rows();
        int index = -1;
        for (int position = 0; position < publishedRows.size(); position += 1) {
            if (publishedRows.get(position).id().equals(row.id())) {
                index = position;
                break;
            }
        }
        ContentDtos.ArticleSummaryResponse previous = index > 0
                ? mapArticleSummary(publishedRows.get(index - 1))
                : null;
        ContentDtos.ArticleSummaryResponse next = index >= 0 && index < publishedRows.size() - 1
                ? mapArticleSummary(publishedRows.get(index + 1))
                : null;
        return new ContentDtos.ArticleDetailResponse(
                row.id(),
                row.title(),
                row.desc(),
                splitTags(row.tags()),
                row.date(),
                row.status(),
                cleanText(row.content(), 100000),
                row.viewCount(),
                stats.visitorCount(),
                previous,
                next,
                relatedArticles(row, publishedRows)
        );
    }

    private List<ContentDtos.ArticleSummaryResponse> relatedArticles(
            ContentRepository.ArticleRow current,
            List<ContentRepository.ArticleRow> candidates
    ) {
        List<String> tags = splitTags(current.tags());
        return candidates.stream()
                .filter(item -> !item.id().equals(current.id()))
                .filter(item -> splitTags(item.tags()).stream().anyMatch(tags::contains))
                .sorted((left, right) -> Long.compare(
                        sharedTagCount(right.tags(), tags),
                        sharedTagCount(left.tags(), tags)
                ))
                .limit(3)
                .map(this::mapArticleSummary)
                .toList();
    }

    private long sharedTagCount(String value, List<String> tags) {
        return splitTags(value).stream().filter(tags::contains).count();
    }

    private ContentDtos.ArticleSummaryResponse mapArticleSummary(ContentRepository.ArticleRow row) {
        return new ContentDtos.ArticleSummaryResponse(
                row.id(),
                row.title(),
                row.desc(),
                splitTags(row.tags()),
                row.date(),
                row.status(),
                row.scheduledAt(),
                row.viewCount()
        );
    }

    private ContentRepository.WorkInput normalizeWorkInput(ContentDtos.WorkRequest request) {
        String tags = joinTags(request.tags());
        String techStack = joinTags(request.techStack());
        requireStoredLength(tags, "标签", 500);
        requireStoredLength(techStack, "技术栈", 500);
        List<String> images = normalizeImageUrls(request.images());

        return new ContentRepository.WorkInput(
                cleanTitle(request.title()),
                cleanUrl(request.cover()),
                cleanText(request.desc(), 2000),
                tags,
                requireYear(request.year()),
                cleanText(request.role(), 100),
                cleanText(request.period(), 100),
                cleanUrl(request.projectUrl()),
                techStack,
                cleanText(request.content(), 100000),
                cleanText(request.result(), 100000),
                String.join("\n", images)
        );
    }

    private ContentDtos.HomeConfigResponse parseHomeConfig(String json) {
        try {
            return objectMapper.readValue(json, ContentDtos.HomeConfigResponse.class);
        } catch (Exception exception) {
            return defaultHomeConfig();
        }
    }

    private String writeHomeConfig(ContentDtos.HomeConfigResponse config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception exception) {
            throw new IllegalStateException("首页配置保存失败", exception);
        }
    }

    private ContentDtos.HomeConfigResponse normalizeHomeConfig(ContentDtos.HomeConfigRequest request) {
        if (request.features() == null || request.features().isEmpty()) {
            throw new IllegalArgumentException("至少配置一条产品特性");
        }
        if (request.features().size() > 12) {
            throw new IllegalArgumentException("产品特性最多12条");
        }

        List<ContentDtos.HomeFeatureResponse> features = request.features().stream()
                .map(feature -> new ContentDtos.HomeFeatureResponse(
                        requireHomeText(feature.title(), "特性标题", 60),
                        cleanText(feature.desc(), 1000),
                        cleanUrl(feature.image())
                ))
                .toList();

        return new ContentDtos.HomeConfigResponse(
                requireHomeText(request.platformName(), "平台名称", 80),
                cleanText(request.heroBadge(), 80),
                requireHomeText(request.heroTitle(), "主标题", 120),
                cleanText(request.heroDescription(), 1000),
                cleanUrl(request.heroVideoUrl()),
                requireHomeText(request.heroPrimaryButtonText(), "主按钮文案", 30),
                requireHomeText(request.heroSecondaryButtonText(), "次按钮文案", 30),
                requireHomeText(request.introTitle(), "介绍标题", 120),
                cleanText(request.introDescription(), 2000),
                cleanUrl(request.introBackgroundUrl()),
                requireHomeText(request.featureTitle(), "特性标题", 80),
                features,
                requireHomeText(request.contactTitle(), "联系标题", 80),
                cleanText(request.contactDescription(), 500),
                requireHomeText(request.contactPhone(), "联系方式", 100),
                cleanText(request.footerText(), 200)
        );
    }

    private String requireHomeText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        if (value.trim().length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "不能超过" + maxLength + "个字符");
        }
        return value.trim();
    }

    private String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        if (value.trim().length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "不能超过" + maxLength + "个字符");
        }
        return value.trim();
    }

    public ContentDtos.HomeConfigResponse defaultHomeConfig() {
        List<ContentDtos.HomeFeatureResponse> features = List.of(
                new ContentDtos.HomeFeatureResponse(
                        "酷炫大屏",
                        "由专业设计师精心预设生产情况总览，政府工作指数，销售实时监控等多种行业模板，丰富的图形组件，使您可以通过图形化的界面在十分钟之内快速搭建起专业、美观、酷炫的大屏展示。",
                        "https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/xvJ7ntosp7BrsXiqbGm5E.png"
                ),
                new ContentDtos.HomeFeatureResponse(
                        "自由布局",
                        "不必预先导入数据，用户可以直接将所要呈现的组件拖拽到画布上进行自由配置和布局，通过点击拖拽即可调整图层顺序，全程图形化编辑操作，所见即所得。",
                        "https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/qaagxLeAKs31tIiylVbnH.png"
                ),
                new ContentDtos.HomeFeatureResponse(
                        "灵活多端",
                        "用户可按实际屏幕尺寸自定义任意画布大小，适配各种拼接大屏场景。大屏以网页形式预览和发布，页面基于 Web 渲染，无需额外安装软件，使用浏览器即可灵活投放至多种屏幕终端。",
                        "https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/F0E8XtBLzoJO_5QCtVFNA.png"
                )
        );
        return new ContentDtos.HomeConfigResponse(
                "FAN · 凡集",
                "数据可视化 · 大屏搭建平台",
                "FAN · 凡集",
                "一站式数据可视化展示平台，拖拽式自由布局、多数据源接入与实时更新，十分钟搭建专业数据大屏。",
                null,
                "进入平台",
                "了解产品",
                "让数据一眼被看见",
                "FAN · 凡集是一站式数据可视化展示平台，旨在帮助用户快速通过可视化图表展示大量数据，低门槛快速打造出专业大屏数据展示。精心预设多种行业模板，极致展示数据魅力。采用拖拽式自由布局，全图形化编辑，快速可视化制作。FAN · 凡集支持多种数据来源配置，支持数据实时同步更新，同时基于 Web 页面渲染，可灵活投屏多种屏幕终端。",
                "https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/fTj6N6HCvoIdYGdjh9eF2.png",
                "产品特性",
                features,
                "联系我们",
                "项目咨询与产品演示，欢迎随时联系",
                "156****7568",
                "© 2026 FAN · 凡集 · 保留所有权利"
        );
    }

    private void ensureWork(String id) {
        if (repository.findWork(id).isEmpty()) {
            throw new IllegalArgumentException("作品不存在");
        }
    }

    private void ensureComic(String id) {
        if (repository.findComic(id).isEmpty()) {
            throw new IllegalArgumentException("漫画不存在");
        }
    }

    private void ensureMovie(String id) {
        if (repository.findMovie(id).isEmpty()) {
            throw new IllegalArgumentException("电影不存在");
        }
    }

    private void ensureAnime(String id) {
        if (repository.findAnime(id).isEmpty()) {
            throw new IllegalArgumentException("动漫不存在");
        }
    }

    private void ensureArticle(String id) {
        if (repository.findArticle(id).isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
    }

    private void validateWork(ContentDtos.WorkRequest request) {
        requireTitle(request.title());
        if (request.year() == null) {
            throw new IllegalArgumentException("年份不能为空");
        }
    }

    private void validateComic(ContentDtos.ComicRequest request) {
        requireTitle(request.title());
        if (request.status() == null || request.status().isBlank()) {
            throw new IllegalArgumentException("状态不能为空");
        }
        cleanText(request.status(), 30);
        requireCount(request.chapters(), "章节数");
    }

    private void validateComicChapter(ContentDtos.ComicChapterRequest request) {
        requireTitle(request.title());
        if (request.chapterNo() != null && request.chapterNo() < 1) {
            throw new IllegalArgumentException("章节序号必须大于0");
        }
        optionalDate(request.date());
    }

    private void validateMovie(ContentDtos.MovieRequest request) {
        requireTitle(request.title());
        if (request.genre() == null || request.genre().isBlank()) {
            throw new IllegalArgumentException("类型不能为空");
        }
        cleanText(request.genre(), 50);
        requireRating(request.rating());
    }

    private void validateAnime(ContentDtos.AnimeRequest request) {
        requireTitle(request.title());
        if (request.genre() == null || request.genre().isBlank()) {
            throw new IllegalArgumentException("类型不能为空");
        }
        if (request.status() == null || request.status().isBlank()) {
            throw new IllegalArgumentException("更新状态不能为空");
        }
        cleanText(request.genre(), 50);
        cleanText(request.status(), 50);
        requireRating(request.rating());
    }

    private void validateArticle(ContentDtos.ArticleRequest request) {
        requireTitle(request.title());
        requireDate(request.date());
        normalizeArticleStatus(request.status());
    }

    private Integer requireScore(Integer value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value < 0 || value > 10) {
            throw new IllegalArgumentException(fieldName + "必须在0到10之间");
        }
        return value;
    }

    private String normalizeArticleStatus(String status) {
        if (status == null || status.isBlank()) {
            return ARTICLE_STATUS_PUBLISHED;
        }
        if (ARTICLE_STATUS_PUBLISHED.equals(status) || ARTICLE_STATUS_DRAFT.equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("文章状态必须是published或draft");
    }

    private ContentRepository.ArticleQuery articleQuery(
            String keyword,
            String tag,
            Integer year,
            PageSelection selection
    ) {
        if (year != null && (year < 1900 || year > 2100)) {
            throw new IllegalArgumentException("年份必须在1900到2100之间");
        }
        return new ContentRepository.ArticleQuery(
                normalizeKeyword(keyword),
                normalizeKeyword(tag),
                year,
                selection.limit(),
                selection.offset()
        );
    }

    private PageSelection normalizePage(Integer page, Integer pageSize) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 50);
        return new PageSelection(normalizedPage, normalizedPageSize);
    }

    private String normalizeKeyword(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private <T> ContentDtos.PageResponse<T> toPage(
            List<T> items,
            int total,
            PageSelection selection
    ) {
        return new ContentDtos.PageResponse<>(items, total, selection.page(), selection.pageSize());
    }

    private List<String> distinctTags(List<String> values) {
        return values.stream()
                .flatMap(value -> splitTags(value).stream())
                .distinct()
                .sorted()
                .toList();
    }

    private record PageSelection(int page, int pageSize) {
        private int limit() {
            return pageSize;
        }

        private int offset() {
            return (page - 1) * pageSize;
        }
    }

    private void requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (title.trim().length() > 150) {
            throw new IllegalArgumentException("标题不能超过150个字符");
        }
    }

    private String cleanTitle(String title) {
        requireTitle(title);
        return title.trim();
    }

    private String cleanText(String value, int maxLength) {
        if (value != null && value.trim().length() > maxLength) {
            throw new IllegalArgumentException("内容长度不能超过" + maxLength + "个字符");
        }
        return value == null ? null : value.trim();
    }

    private String cleanUrl(String value) {
        if (value != null && value.trim().length() > 1000) {
            throw new IllegalArgumentException("链接不能超过1000个字符");
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.startsWith("/")) {
            throw new IllegalArgumentException("链接必须以http://、https://或/开头");
        }
        return trimmed;
    }

    private List<String> normalizeImageUrls(List<String> values) {
        if (values == null) {
            return List.of();
        }
        if (values.size() > 12) {
            throw new IllegalArgumentException("截图最多12张");
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::cleanUrl)
                .toList();
    }

    private void requireStoredLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "不能超过" + maxLength + "个字符");
        }
    }

    private List<String> splitTags(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("[,，]"));
    }

    private List<String> splitLines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return value.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    private String joinLines(List<String> values) {
        List<String> normalized = values == null ? List.of() : values;
        return normalized.isEmpty() ? null : String.join("\n", normalized);
    }

    private String joinTags(List<String> tags) {
        List<String> normalized = normalizeTags(tags);
        return normalized.isEmpty() ? null : String.join(",", normalized);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .flatMap(tag -> List.of(tag.split("[,，]")).stream())
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();
    }

    private int requireYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("年份不能为空");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("年份必须在1900到2100之间");
        }
        return year;
    }

    private int requireCount(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        if (value < 0 || value > 100000) {
            throw new IllegalArgumentException(fieldName + "必须在0到100000之间");
        }
        return value;
    }

    private BigDecimal requireRating(BigDecimal rating) {
        if (rating == null) {
            throw new IllegalArgumentException("评分不能为空");
        }
        BigDecimal value = rating.setScale(1, RoundingMode.HALF_UP);
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("评分必须在0到10之间");
        }
        return value;
    }

    private String requireDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("日期不能为空");
        }
        try {
            return LocalDate.parse(date).toString();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("日期格式必须为YYYY-MM-DD");
        }
    }

    private String optionalDate(String date) {
        return date == null || date.isBlank() ? null : requireDate(date);
    }
}
