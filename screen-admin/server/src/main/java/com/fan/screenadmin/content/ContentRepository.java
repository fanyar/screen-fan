package com.fan.screenadmin.content;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ContentRepository {

    private static final RowMapper<WorkRow> WORK_ROW_MAPPER = ContentRepository::mapWork;
    private static final RowMapper<ComicRow> COMIC_ROW_MAPPER = ContentRepository::mapComic;
    private static final RowMapper<MovieRow> MOVIE_ROW_MAPPER = ContentRepository::mapMovie;
    private static final RowMapper<AnimeRow> ANIME_ROW_MAPPER = ContentRepository::mapAnime;
    private static final RowMapper<ArticleRow> ARTICLE_ROW_MAPPER = ContentRepository::mapArticle;
    private static final Map<String, String> CONTENT_ACCESS_TABLES = Map.of(
            "works", "work_item",
            "comics", "comic_item",
            "movies", "movie_item",
            "anime", "anime_item",
            "articles", "article_item"
    );

    private final JdbcTemplate jdbcTemplate;

    public ContentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WorkPage pageWorks(WorkQuery query) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        addKeywordCondition(
                conditions,
                args,
                query.keyword(),
                "title",
                "description",
                "tags",
                "role",
                "period",
                "tech_stack"
        );
        addLikeCondition(conditions, args, "tags", query.tag());
        addEqualsCondition(conditions, args, "release_year", query.year());

        List<WorkRow> rows = jdbcTemplate.query(
                "SELECT id, title, cover_url, description, tags, release_year, role, period, project_url, tech_stack, content, result, images FROM work_item"
                        + conditions + " ORDER BY release_year DESC, sort_no, title LIMIT ? OFFSET ?",
                WORK_ROW_MAPPER,
                withPageArgs(args, query)
        );
        int total = countRows("work_item", conditions, args);
        return new WorkPage(rows, total);
    }

    public Optional<WorkRow> findWork(String id) {
        return jdbcTemplate.query(
                "SELECT id, title, cover_url, description, tags, release_year, role, period, project_url, tech_stack, content, result, images FROM work_item WHERE id = ?",
                WORK_ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public void insertWork(WorkInput input, String id, int sortNo) {
        jdbcTemplate.update(
                """
                        INSERT INTO work_item(
                            id, title, cover_url, description, tags, release_year, role, period,
                            project_url, tech_stack, content, result, images, sort_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                input.title(),
                input.cover(),
                input.desc(),
                input.tags(),
                input.year(),
                input.role(),
                input.period(),
                input.projectUrl(),
                input.techStack(),
                input.content(),
                input.result(),
                input.images(),
                sortNo
        );
    }

    public int updateWork(String id, WorkInput input) {
        return jdbcTemplate.update(
                """
                        UPDATE work_item
                        SET title = ?, cover_url = ?, description = ?, tags = ?, release_year = ?,
                            role = ?, period = ?, project_url = ?, tech_stack = ?,
                            content = ?, result = ?, images = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                input.title(),
                input.cover(),
                input.desc(),
                input.tags(),
                input.year(),
                input.role(),
                input.period(),
                input.projectUrl(),
                input.techStack(),
                input.content(),
                input.result(),
                input.images(),
                id
        );
    }

    public int deleteWork(String id) {
        return jdbcTemplate.update("DELETE FROM work_item WHERE id = ?", id);
    }

    public ComicPage pageComics(ComicQuery query) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        addKeywordCondition(conditions, args, query.keyword(), "title", "description", "status");
        addEqualsCondition(conditions, args, "status", query.status());

        List<ComicRow> rows = jdbcTemplate.query(
                "SELECT id, title, cover_url, status, chapters, description FROM comic_item"
                        + conditions + " ORDER BY sort_no, title LIMIT ? OFFSET ?",
                COMIC_ROW_MAPPER,
                withPageArgs(args, query)
        );
        int total = countRows("comic_item", conditions, args);
        return new ComicPage(rows, total);
    }

    public Optional<ComicRow> findComic(String id) {
        return jdbcTemplate.query(
                "SELECT id, title, cover_url, status, chapters, description FROM comic_item WHERE id = ?",
                COMIC_ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public void insertComic(String id, String title, String cover, String status, int chapters, String desc, int sortNo) {
        jdbcTemplate.update(
                "INSERT INTO comic_item(id, title, cover_url, status, chapters, description, sort_no) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, title, cover, status, chapters, desc, sortNo
        );
    }

    public int updateComic(String id, String title, String cover, String status, int chapters, String desc) {
        return jdbcTemplate.update(
                "UPDATE comic_item SET title = ?, cover_url = ?, status = ?, chapters = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                title, cover, status, chapters, desc, id
        );
    }

    public int deleteComic(String id) {
        return jdbcTemplate.update("DELETE FROM comic_item WHERE id = ?", id);
    }

    public MoviePage pageMovies(MovieQuery query) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        addKeywordCondition(conditions, args, query.keyword(), "title", "genre", "note", "director", "cast_names", "review");
        addEqualsCondition(conditions, args, "genre", query.genre());
        addEqualsCondition(conditions, args, "release_year", query.year());

        List<MovieRow> rows = jdbcTemplate.query(
                """
                        SELECT id, title, cover_url, play_url, release_year, rating, genre, note, watch_date,
                               director, cast_names, review, story_score, visual_score, sound_score, rewatch_value
                        FROM movie_item
                        """
                        + conditions + " ORDER BY release_year DESC, sort_no, title LIMIT ? OFFSET ?",
                MOVIE_ROW_MAPPER,
                withPageArgs(args, query)
        );
        int total = countRows("movie_item", conditions, args);
        return new MoviePage(rows, total);
    }

    public Optional<MovieRow> findMovie(String id) {
        return jdbcTemplate.query(
                """
                        SELECT id, title, cover_url, play_url, release_year, rating, genre, note, watch_date,
                               director, cast_names, review, story_score, visual_score, sound_score, rewatch_value
                        FROM movie_item WHERE id = ?
                        """,
                MOVIE_ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public void insertMovie(MovieInput input, String id, int sortNo) {
        jdbcTemplate.update(
                """
                        INSERT INTO movie_item(
                            id, title, cover_url, play_url, release_year, rating, genre, note, watch_date,
                            director, cast_names, review, story_score, visual_score, sound_score, rewatch_value, sort_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                input.title(),
                input.cover(),
                input.playUrl(),
                input.year(),
                input.rating(),
                input.genre(),
                input.note(),
                input.watchDate() == null ? null : java.sql.Date.valueOf(input.watchDate()),
                input.director(),
                input.castNames(),
                input.review(),
                input.storyScore(),
                input.visualScore(),
                input.soundScore(),
                input.rewatchValue(),
                sortNo
        );
    }

    public int updateMovie(String id, MovieInput input) {
        return jdbcTemplate.update(
                """
                        UPDATE movie_item
                        SET title = ?, cover_url = ?, play_url = ?, release_year = ?, rating = ?, genre = ?, note = ?,
                            watch_date = ?, director = ?, cast_names = ?, review = ?,
                            story_score = ?, visual_score = ?, sound_score = ?, rewatch_value = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                input.title(),
                input.cover(),
                input.playUrl(),
                input.year(),
                input.rating(),
                input.genre(),
                input.note(),
                input.watchDate() == null ? null : java.sql.Date.valueOf(input.watchDate()),
                input.director(),
                input.castNames(),
                input.review(),
                input.storyScore(),
                input.visualScore(),
                input.soundScore(),
                input.rewatchValue(),
                id
        );
    }

    public int deleteMovie(String id) {
        return jdbcTemplate.update("DELETE FROM movie_item WHERE id = ?", id);
    }

    public AnimePage pageAnime(AnimeQuery query) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        addKeywordCondition(conditions, args, query.keyword(), "title", "genre", "status", "note", "review");
        addEqualsCondition(conditions, args, "genre", query.genre());
        addEqualsCondition(conditions, args, "release_year", query.year());

        List<AnimeRow> rows = jdbcTemplate.query(
                """
                        SELECT id, title, cover_url, play_url, release_year, rating, genre, status, episodes, note, review
                        FROM anime_item
                        """
                        + conditions + " ORDER BY release_year DESC, sort_no, title LIMIT ? OFFSET ?",
                ANIME_ROW_MAPPER,
                withPageArgs(args, query)
        );
        int total = countRows("anime_item", conditions, args);
        return new AnimePage(rows, total);
    }

    public Optional<AnimeRow> findAnime(String id) {
        return jdbcTemplate.query(
                """
                        SELECT id, title, cover_url, play_url, release_year, rating, genre, status, episodes, note, review
                        FROM anime_item WHERE id = ?
                        """,
                ANIME_ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public void insertAnime(AnimeInput input, String id, int sortNo) {
        jdbcTemplate.update(
                """
                        INSERT INTO anime_item(
                            id, title, cover_url, play_url, release_year, rating, genre, status, episodes, note, review, sort_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                input.title(),
                input.cover(),
                input.playUrl(),
                input.year(),
                input.rating(),
                input.genre(),
                input.status(),
                input.episodes(),
                input.note(),
                input.review(),
                sortNo
        );
    }

    public int updateAnime(String id, AnimeInput input) {
        return jdbcTemplate.update(
                """
                        UPDATE anime_item
                        SET title = ?, cover_url = ?, play_url = ?, release_year = ?, rating = ?, genre = ?, status = ?,
                            episodes = ?, note = ?, review = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                input.title(),
                input.cover(),
                input.playUrl(),
                input.year(),
                input.rating(),
                input.genre(),
                input.status(),
                input.episodes(),
                input.note(),
                input.review(),
                id
        );
    }

    public int deleteAnime(String id) {
        return jdbcTemplate.update("DELETE FROM anime_item WHERE id = ?", id);
    }

    public ArticlePage pageArticles(ArticleQuery query, boolean publishedOnly) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        if (publishedOnly) {
            conditions.append(" AND status = 'published' AND (scheduled_at IS NULL OR scheduled_at <= CURRENT_TIMESTAMP)");
        }
        List<Object> args = new ArrayList<>();
        addKeywordCondition(conditions, args, query.keyword(), "title", "description", "tags");
        addLikeCondition(conditions, args, "tags", query.tag());
        addEqualsCondition(conditions, args, "YEAR(publish_date)", query.year());

        List<ArticleRow> rows = jdbcTemplate.query(
                "SELECT id, title, description, tags, publish_date, content, status, scheduled_at, view_count FROM article_item"
                        + conditions + " ORDER BY sort_no, publish_date DESC, title LIMIT ? OFFSET ?",
                ARTICLE_ROW_MAPPER,
                withPageArgs(args, query)
        );
        int total = countArticles(query, publishedOnly);
        return new ArticlePage(rows, total);
    }

    public int countArticles(ArticleQuery query, boolean publishedOnly) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        if (publishedOnly) {
            conditions.append(" AND status = 'published' AND (scheduled_at IS NULL OR scheduled_at <= CURRENT_TIMESTAMP)");
        }
        List<Object> args = new ArrayList<>();
        addKeywordCondition(conditions, args, query.keyword(), "title", "description", "tags");
        addLikeCondition(conditions, args, "tags", query.tag());
        addEqualsCondition(conditions, args, "YEAR(publish_date)", query.year());
        return countRows("article_item", conditions, args);
    }

    public List<String> listWorkTags() {
        return listTags("work_item");
    }

    public List<Integer> listWorkYears() {
        return jdbcTemplate.query(
                "SELECT DISTINCT release_year FROM work_item ORDER BY release_year DESC",
                (rs, rowNum) -> rs.getInt("release_year")
        );
    }

    public List<String> listComicStatuses() {
        return jdbcTemplate.query(
                "SELECT DISTINCT status FROM comic_item ORDER BY status",
                (rs, rowNum) -> rs.getString("status")
        );
    }

    public List<String> listMovieGenres() {
        return jdbcTemplate.query(
                "SELECT DISTINCT genre FROM movie_item ORDER BY genre",
                (rs, rowNum) -> rs.getString("genre")
        );
    }

    public List<Integer> listMovieYears() {
        return jdbcTemplate.query(
                "SELECT DISTINCT release_year FROM movie_item ORDER BY release_year DESC",
                (rs, rowNum) -> rs.getInt("release_year")
        );
    }

    public List<String> listAnimeGenres() {
        return jdbcTemplate.query(
                "SELECT DISTINCT genre FROM anime_item ORDER BY genre",
                (rs, rowNum) -> rs.getString("genre")
        );
    }

    public List<Integer> listAnimeYears() {
        return jdbcTemplate.query(
                "SELECT DISTINCT release_year FROM anime_item ORDER BY release_year DESC",
                (rs, rowNum) -> rs.getInt("release_year")
        );
    }

    public List<String> listArticleTags() {
        return jdbcTemplate.query(
                "SELECT tags FROM article_item WHERE status = 'published' AND tags IS NOT NULL AND tags <> ''",
                (rs, rowNum) -> rs.getString("tags")
        );
    }

    public List<Integer> listArticleYears() {
        return jdbcTemplate.query(
                "SELECT DISTINCT YEAR(publish_date) AS publish_year FROM article_item WHERE status = 'published' ORDER BY publish_year DESC",
                (rs, rowNum) -> rs.getInt("publish_year")
        );
    }

    public List<ContactMessageRow> listContactMessages() {
        return jdbcTemplate.query(
                "SELECT id, name, contact, content, handled, created_at FROM contact_message ORDER BY created_at DESC",
                (rs, rowNum) -> new ContactMessageRow(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("content"),
                        rs.getBoolean("handled"),
                        rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
        );
    }

    public void insertContactMessage(String id, String name, String contact, String content) {
        jdbcTemplate.update(
                "INSERT INTO contact_message(id, name, contact, content) VALUES (?, ?, ?, ?)",
                id,
                name,
                contact,
                content
        );
    }

    public int updateContactMessageHandled(String id, boolean handled) {
        return jdbcTemplate.update("UPDATE contact_message SET handled = ? WHERE id = ?", handled, id);
    }

    public int deleteContactMessage(String id) {
        return jdbcTemplate.update("DELETE FROM contact_message WHERE id = ?", id);
    }

    public Optional<ArticleRow> findArticle(String id) {
        return jdbcTemplate.query(
                "SELECT id, title, description, tags, publish_date, content, status, scheduled_at, view_count FROM article_item WHERE id = ?",
                ARTICLE_ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public int incrementArticleView(String id) {
        return jdbcTemplate.update(
                "UPDATE article_item SET view_count = view_count + 1 WHERE id = ? AND status = 'published'",
                id
        );
    }

    @Transactional
    public ContentViewStats recordContentView(String contentType, String contentId, String visitorKey) {
        String table = CONTENT_ACCESS_TABLES.get(contentType);
        if (table == null) {
            return new ContentViewStats(0, 0);
        }
        String normalizedVisitorKey = normalizeVisitorKey(visitorKey);
        int updated = jdbcTemplate.update(
                "UPDATE " + table + " SET view_count = view_count + 1 WHERE id = ?",
                contentId
        );
        if (updated == 0) {
            return new ContentViewStats(0, 0);
        }

        int knownVisitor = jdbcTemplate.update(
                """
                        UPDATE content_visitor_log
                        SET view_count = view_count + 1, last_viewed_at = CURRENT_TIMESTAMP
                        WHERE content_type = ? AND content_id = ? AND visitor_key = ?
                        """,
                contentType,
                contentId,
                normalizedVisitorKey
        );
        if (knownVisitor == 0) {
            jdbcTemplate.update(
                    """
                            INSERT INTO content_visitor_log(content_type, content_id, visitor_key, view_count)
                            VALUES (?, ?, ?, 1)
                            """,
                    contentType,
                    contentId,
                    normalizedVisitorKey
            );
            jdbcTemplate.update(
                    "UPDATE " + table + " SET visitor_count = visitor_count + 1 WHERE id = ?",
                    contentId
            );
        }

        return getViewStats(contentType, contentId);
    }

    public ContentViewStats getViewStats(String contentType, String contentId) {
        String table = CONTENT_ACCESS_TABLES.get(contentType);
        if (table == null) {
            return new ContentViewStats(0, 0);
        }
        List<ContentViewStats> rows = jdbcTemplate.query(
                "SELECT view_count, visitor_count FROM " + table + " WHERE id = ?",
                (rs, rowNum) -> new ContentViewStats(rs.getLong("view_count"), rs.getLong("visitor_count")),
                contentId
        );
        return rows.stream().findFirst().orElse(new ContentViewStats(0, 0));
    }

    private String normalizeVisitorKey(String value) {
        String key = value == null ? "" : value.trim().replaceAll("[^a-zA-Z0-9_-]", "");
        if (key.isBlank()) {
            return "anonymous";
        }
        return key.length() > 64 ? key.substring(0, 64) : key;
    }

    public void insertArticle(
            String id,
            String title,
            String desc,
            String tags,
            String date,
            String content,
            String status,
            String scheduledAt,
            int sortNo
    ) {
        jdbcTemplate.update(
                "INSERT INTO article_item(id, title, description, tags, publish_date, content, status, scheduled_at, sort_no) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, title, desc, tags, java.sql.Date.valueOf(date), content, status,
                scheduledAt == null || scheduledAt.isBlank() ? null : java.sql.Timestamp.valueOf(scheduledAt),
                sortNo
        );
    }

    public int updateArticle(
            String id,
            String title,
            String desc,
            String tags,
            String date,
            String content,
            String status,
            String scheduledAt
    ) {
        return jdbcTemplate.update(
                "UPDATE article_item SET title = ?, description = ?, tags = ?, publish_date = ?, content = ?, status = ?, scheduled_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                title, desc, tags, java.sql.Date.valueOf(date), content, status,
                scheduledAt == null || scheduledAt.isBlank() ? null : java.sql.Timestamp.valueOf(scheduledAt),
                id
        );
    }

    public int deleteArticle(String id) {
        return jdbcTemplate.update("DELETE FROM article_item WHERE id = ?", id);
    }

    public Optional<String> findHomeConfig() {
        List<String> values = jdbcTemplate.query(
                "SELECT config_json FROM home_page_config WHERE config_key = 'default'",
                (rs, rowNum) -> rs.getString("config_json")
        );
        return values.stream().findFirst();
    }

    public void saveHomeConfig(String configJson) {
        int updated = jdbcTemplate.update(
                "UPDATE home_page_config SET config_json = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = 'default'",
                configJson
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO home_page_config(config_key, config_json) VALUES ('default', ?)",
                    configJson
            );
        }
    }

    public int countWorks() {
        return count("work_item");
    }

    public int countComics() {
        return count("comic_item");
    }

    public int countMovies() {
        return count("movie_item");
    }

    public int countAnime() {
        return count("anime_item");
    }

    public int countArticles() {
        return count("article_item");
    }

    public long countPublishedArticles() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM article_item WHERE status = 'published'",
                Long.class
        );
        return value == null ? 0 : value;
    }

    public long countDraftArticles() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM article_item WHERE status = 'draft'",
                Long.class
        );
        return value == null ? 0 : value;
    }

    public long sumArticleViews() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(view_count), 0) FROM article_item",
                Long.class
        );
        return value == null ? 0 : value;
    }

    public long countUnreadMessages() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM contact_message WHERE handled = FALSE",
                Long.class
        );
        return value == null ? 0 : value;
    }

    public List<ArticleRow> listPopularArticles(int limit) {
        return jdbcTemplate.query(
                """
                        SELECT id, title, description, tags, publish_date, content, status, scheduled_at, view_count
                        FROM article_item WHERE status = 'published'
                        ORDER BY view_count DESC, publish_date DESC, title LIMIT ?
                        """,
                ARTICLE_ROW_MAPPER,
                limit
        );
    }

    public int nextWorkSortNo() {
        return previousSortNo("work_item");
    }

    public int nextComicSortNo() {
        return previousSortNo("comic_item");
    }

    public int nextMovieSortNo() {
        return previousSortNo("movie_item");
    }

    public int nextAnimeSortNo() {
        return previousSortNo("anime_item");
    }

    public int nextArticleSortNo() {
        return previousSortNo("article_item");
    }

    private int count(String table) {
        Integer value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return value == null ? 0 : value;
    }

    private int previousSortNo(String table) {
        Integer value = jdbcTemplate.queryForObject("SELECT MIN(sort_no) FROM " + table, Integer.class);
        return value == null ? 1 : value - 1;
    }

    private int countRows(String table, StringBuilder conditions, List<Object> args) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + conditions,
                Integer.class,
                args.toArray()
        );
        return value == null ? 0 : value;
    }

    private void addKeywordCondition(
            StringBuilder conditions,
            List<Object> args,
            String keyword,
            String... fields
    ) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        conditions.append(" AND (");
        for (int index = 0; index < fields.length; index += 1) {
            if (index > 0) {
                conditions.append(" OR ");
            }
            conditions.append("LOWER(COALESCE(").append(fields[index]).append(", '')) LIKE ?");
            args.add("%" + keyword.toLowerCase() + "%");
        }
        conditions.append(")");
    }

    private void addLikeCondition(StringBuilder conditions, List<Object> args, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        conditions.append(" AND LOWER(COALESCE(").append(field).append(", '')) LIKE ?");
        args.add("%" + value.toLowerCase() + "%");
    }

    private void addEqualsCondition(StringBuilder conditions, List<Object> args, String field, Object value) {
        if (value == null || value instanceof String text && text.isBlank()) {
            return;
        }
        conditions.append(" AND ").append(field).append(" = ?");
        args.add(value);
    }

    private Object[] withPageArgs(List<Object> args, Pageable query) {
        List<Object> values = new ArrayList<>(args);
        values.add(query.limit());
        values.add(query.offset());
        return values.toArray();
    }

    private List<String> listTags(String table) {
        return jdbcTemplate.query(
                "SELECT tags FROM " + table + " WHERE tags IS NOT NULL AND tags <> ''",
                (rs, rowNum) -> rs.getString("tags")
        );
    }

    private static WorkRow mapWork(ResultSet rs, int rowNum) throws SQLException {
        return new WorkRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getString("description"),
                rs.getString("tags"),
                rs.getInt("release_year"),
                rs.getString("role"),
                rs.getString("period"),
                rs.getString("project_url"),
                rs.getString("tech_stack"),
                rs.getString("content"),
                rs.getString("result"),
                rs.getString("images")
        );
    }

    private static ComicRow mapComic(ResultSet rs, int rowNum) throws SQLException {
        return new ComicRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getString("status"),
                rs.getInt("chapters"),
                rs.getString("description")
        );
    }

    private static MovieRow mapMovie(ResultSet rs, int rowNum) throws SQLException {
        return new MovieRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getString("play_url"),
                rs.getInt("release_year"),
                rs.getBigDecimal("rating"),
                rs.getString("genre"),
                rs.getString("note"),
                rs.getDate("watch_date") == null ? null : rs.getDate("watch_date").toLocalDate().toString(),
                rs.getString("director"),
                rs.getString("cast_names"),
                rs.getString("review"),
                rs.getObject("story_score") == null ? null : rs.getInt("story_score"),
                rs.getObject("visual_score") == null ? null : rs.getInt("visual_score"),
                rs.getObject("sound_score") == null ? null : rs.getInt("sound_score"),
                rs.getObject("rewatch_value") == null ? null : rs.getInt("rewatch_value")
        );
    }

    private static AnimeRow mapAnime(ResultSet rs, int rowNum) throws SQLException {
        return new AnimeRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getString("play_url"),
                rs.getInt("release_year"),
                rs.getBigDecimal("rating"),
                rs.getString("genre"),
                rs.getString("status"),
                rs.getInt("episodes"),
                rs.getString("note"),
                rs.getString("review")
        );
    }

    private static ArticleRow mapArticle(ResultSet rs, int rowNum) throws SQLException {
        return new ArticleRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("tags"),
                rs.getDate("publish_date").toLocalDate().toString(),
                rs.getString("content"),
                rs.getString("status"),
                rs.getString("scheduled_at"),
                rs.getLong("view_count")
        );
    }

    private interface Pageable {
        int limit();

        int offset();
    }

    public record WorkRow(
            String id,
            String title,
            String cover,
            String desc,
            String tags,
            int year,
            String role,
            String period,
            String projectUrl,
            String techStack,
            String content,
            String result,
            String images
    ) {
    }

    public record WorkInput(
            String title,
            String cover,
            String desc,
            String tags,
            int year,
            String role,
            String period,
            String projectUrl,
            String techStack,
            String content,
            String result,
            String images
    ) {
    }

    public record ComicRow(String id, String title, String cover, String status, int chapters, String desc) {
    }

    public record MovieRow(
            String id,
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String note,
            String watchDate,
            String director,
            String castNames,
            String review,
            Integer storyScore,
            Integer visualScore,
            Integer soundScore,
            Integer rewatchValue
    ) {
    }

    public record MovieInput(
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String note,
            String watchDate,
            String director,
            String castNames,
            String review,
            Integer storyScore,
            Integer visualScore,
            Integer soundScore,
            Integer rewatchValue
    ) {
    }

    public record AnimeRow(
            String id,
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String status,
            int episodes,
            String note,
            String review
    ) {
    }

    public record AnimeInput(
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String status,
            int episodes,
            String note,
            String review
    ) {
    }

    public record ArticleRow(
            String id,
            String title,
            String desc,
            String tags,
            String date,
            String content,
            String status,
            String scheduledAt,
            long viewCount
    ) {
    }

    public record ContactMessageRow(
            String id,
            String name,
            String contact,
            String content,
            boolean handled,
            String createdAt
    ) {
    }

    public record WorkQuery(
            String keyword,
            String tag,
            Integer year,
            int limit,
            int offset
    ) implements Pageable {
    }

    public record ComicQuery(
            String keyword,
            String status,
            int limit,
            int offset
    ) implements Pageable {
    }

    public record MovieQuery(
            String keyword,
            String genre,
            Integer year,
            int limit,
            int offset
    ) implements Pageable {
    }

    public record AnimeQuery(
            String keyword,
            String genre,
            Integer year,
            int limit,
            int offset
    ) implements Pageable {
    }

    public record ArticleQuery(
            String keyword,
            String tag,
            Integer year,
            int limit,
            int offset
    ) implements Pageable {
    }

    public record WorkPage(List<WorkRow> rows, int total) {
    }

    public record ComicPage(List<ComicRow> rows, int total) {
    }

    public record MoviePage(List<MovieRow> rows, int total) {
    }

    public record AnimePage(List<AnimeRow> rows, int total) {
    }

    public record ArticlePage(List<ArticleRow> rows, int total) {
    }

    public record ContentViewStats(long viewCount, long visitorCount) {
    }

    public record CommentRow(
            String id,
            String contentType,
            String contentId,
            String visitorKey,
            String authorName,
            String body,
            String status,
            String createdAt
    ) {
    }

    private static final RowMapper<CommentRow> COMMENT_ROW_MAPPER = ContentRepository::mapComment;

    private static CommentRow mapComment(ResultSet rs, int rowNum) throws SQLException {
        return new CommentRow(
                rs.getString("id"),
                rs.getString("content_type"),
                rs.getString("content_id"),
                rs.getString("visitor_key"),
                rs.getString("author_name"),
                rs.getString("body"),
                rs.getString("status"),
                rs.getString("created_at")
        );
    }

    public List<CommentRow> listApprovedComments(String contentType, String contentId) {
        return jdbcTemplate.query(
                "SELECT * FROM content_comment WHERE content_type = ? AND content_id = ? AND status = 'approved' ORDER BY created_at DESC",
                COMMENT_ROW_MAPPER,
                contentType,
                contentId
        );
    }

    public List<CommentRow> listAllComments() {
        return jdbcTemplate.query(
                "SELECT * FROM content_comment ORDER BY created_at DESC",
                COMMENT_ROW_MAPPER
        );
    }

    public void insertComment(String id, String contentType, String contentId, String visitorKey, String authorName, String body, String status) {
        jdbcTemplate.update(
                "INSERT INTO content_comment(id, content_type, content_id, visitor_key, author_name, body, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id,
                contentType,
                contentId,
                visitorKey,
                authorName,
                body,
                status
        );
    }

    public int updateCommentStatus(String id, String status) {
        return jdbcTemplate.update(
                "UPDATE content_comment SET status = ? WHERE id = ?",
                status,
                id
        );
    }

    public int deleteComment(String id) {
        return jdbcTemplate.update("DELETE FROM content_comment WHERE id = ?", id);
    }

    public long countCommentsByStatus(String status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM content_comment WHERE status = ?",
                Long.class,
                status
        );
        return count == null ? 0 : count;
    }

    public record ReadingProgressData(
            String comicId,
            String chapterId,
            int chapterNo,
            String updatedAt
    ) {
    }

    public Optional<ReadingProgressData> findReadingProgress(String visitorKey, String comicId) {
        return jdbcTemplate.query(
                """
                SELECT comic_id, chapter_id, chapter_no, updated_at
                FROM comic_reading_progress
                WHERE visitor_key = ? AND comic_id = ?
                """,
                (rs, rowNum) -> new ReadingProgressData(
                        rs.getString("comic_id"),
                        rs.getString("chapter_id"),
                        rs.getInt("chapter_no"),
                        rs.getString("updated_at")
                ),
                visitorKey,
                comicId
        ).stream().findFirst();
    }

    public void upsertReadingProgress(String visitorKey, String comicId, String chapterId, int chapterNo) {
        jdbcTemplate.update(
                """
                MERGE INTO comic_reading_progress(visitor_key, comic_id, chapter_id, chapter_no, updated_at)
                KEY(visitor_key, comic_id)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                visitorKey,
                comicId,
                chapterId,
                chapterNo
        );
    }
}
