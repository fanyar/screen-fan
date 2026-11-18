package com.fan.screenadmin.content;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ComicChapterRepository {

    private final JdbcTemplate jdbcTemplate;

    public ComicChapterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ComicChapterRow> list(String comicId) {
        return jdbcTemplate.query(
                "SELECT id, comic_id, chapter_no, title, summary, pages, published_date FROM comic_chapter WHERE comic_id = ? ORDER BY chapter_no, title",
                (rs, rowNum) -> new ComicChapterRow(
                        rs.getString("id"),
                        rs.getString("comic_id"),
                        rs.getInt("chapter_no"),
                        rs.getString("title"),
                        rs.getString("summary"),
                        rs.getString("pages"),
                        rs.getDate("published_date") == null ? null : rs.getDate("published_date").toLocalDate().toString()
                ),
                comicId
        );
    }

    public Optional<ComicChapterRow> find(String id) {
        return jdbcTemplate.query(
                "SELECT id, comic_id, chapter_no, title, summary, pages, published_date FROM comic_chapter WHERE id = ?",
                (rs, rowNum) -> new ComicChapterRow(
                        rs.getString("id"),
                        rs.getString("comic_id"),
                        rs.getInt("chapter_no"),
                        rs.getString("title"),
                        rs.getString("summary"),
                        rs.getString("pages"),
                        rs.getDate("published_date") == null ? null : rs.getDate("published_date").toLocalDate().toString()
                ),
                id
        ).stream().findFirst();
    }

    public void insert(
            String id,
            String comicId,
            int chapterNo,
            String title,
            String summary,
            String pages,
            String date
    ) {
        jdbcTemplate.update(
                "INSERT INTO comic_chapter(id, comic_id, chapter_no, title, summary, pages, published_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id,
                comicId,
                chapterNo,
                title,
                summary,
                pages,
                date == null ? null : java.sql.Date.valueOf(date)
        );
    }

    public int update(String id, int chapterNo, String title, String summary, String pages, String date) {
        return jdbcTemplate.update(
                "UPDATE comic_chapter SET chapter_no = ?, title = ?, summary = ?, pages = ?, published_date = ? WHERE id = ?",
                chapterNo,
                title,
                summary,
                pages,
                date == null ? null : java.sql.Date.valueOf(date),
                id
        );
    }

    public int delete(String id) {
        return jdbcTemplate.update("DELETE FROM comic_chapter WHERE id = ?", id);
    }

    public int nextChapterNo(String comicId) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(chapter_no), 0) + 1 FROM comic_chapter WHERE comic_id = ?",
                Integer.class,
                comicId
        );
        return value == null ? 1 : value;
    }

    public int count(String comicId) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comic_chapter WHERE comic_id = ?",
                Integer.class,
                comicId
        );
        return value == null ? 0 : value;
    }

    public void syncComicChapterCount(String comicId) {
        jdbcTemplate.update(
                "UPDATE comic_item SET chapters = (SELECT COUNT(*) FROM comic_chapter WHERE comic_id = ?), updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                comicId,
                comicId
        );
    }

    public record ComicChapterRow(
            String id,
            String comicId,
            int chapterNo,
            String title,
            String summary,
            String pages,
            String date
    ) {
    }
}
