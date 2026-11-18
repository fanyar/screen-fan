package com.fan.screenadmin.model;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ModelRepository {

    private static final RowMapper<ModelRow> ROW_MAPPER = ModelRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public ModelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ModelPage page(ModelQuery query) {
        StringBuilder conditions = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (query.publishedOnly()) {
            conditions.append(" AND status = 'published'");
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            conditions.append(" AND (LOWER(COALESCE(title, '')) LIKE ? OR LOWER(COALESCE(description, '')) LIKE ? OR LOWER(COALESCE(tags, '')) LIKE ?)");
            String keyword = "%" + query.keyword().toLowerCase() + "%";
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }
        if (query.category() != null && !query.category().isBlank()) {
            conditions.append(" AND category = ?");
            args.add(query.category());
        }

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(query.limit());
        pageArgs.add(query.offset());
        List<ModelRow> rows = jdbcTemplate.query(
                "SELECT * FROM model_item" + conditions + " ORDER BY sort_no, updated_at DESC, title LIMIT ? OFFSET ?",
                ROW_MAPPER,
                pageArgs.toArray()
        );
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM model_item" + conditions,
                Long.class,
                args.toArray()
        );
        return new ModelPage(rows, total == null ? 0 : total);
    }

    public Optional<ModelRow> find(String id) {
        return jdbcTemplate.query("SELECT * FROM model_item WHERE id = ?", ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    public void insert(ModelInput input, String id) {
        jdbcTemplate.update(
                """
                        INSERT INTO model_item(
                            id, title, category, cover_url, file_url, description, tags, status, sort_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                input.title(),
                input.category(),
                input.cover(),
                input.fileUrl(),
                input.desc(),
                input.tags(),
                input.status(),
                input.sortNo()
        );
    }

    public int update(String id, ModelInput input) {
        return jdbcTemplate.update(
                """
                        UPDATE model_item
                        SET title = ?, category = ?, cover_url = ?, file_url = ?, description = ?,
                            tags = ?, status = ?, sort_no = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                input.title(),
                input.category(),
                input.cover(),
                input.fileUrl(),
                input.desc(),
                input.tags(),
                input.status(),
                input.sortNo(),
                id
        );
    }

    public int delete(String id) {
        return jdbcTemplate.update("DELETE FROM model_item WHERE id = ?", id);
    }

    public int count() {
        Integer value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM model_item", Integer.class);
        return value == null ? 0 : value;
    }

    public boolean existsByTitle(String title) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM model_item WHERE title = ?",
                Integer.class,
                title
        );
        return value != null && value > 0;
    }

    public Optional<String> findIdByTitle(String title) {
        return jdbcTemplate.query(
                "SELECT id FROM model_item WHERE title = ? LIMIT 1",
                (rs, rowNum) -> rs.getString("id"),
                title
        ).stream().findFirst();
    }

    public int deleteByTitle(String title) {
        return jdbcTemplate.update("DELETE FROM model_item WHERE title = ?", title);
    }

    public int updateDescriptionAndTagsByTitle(String title, String description, String tags) {
        return jdbcTemplate.update(
                """
                        UPDATE model_item
                        SET description = ?, tags = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE title = ?
                        """,
                description,
                tags,
                title
        );
    }

    public int nextSortNo() {
        Integer value = jdbcTemplate.queryForObject("SELECT MIN(sort_no) FROM model_item", Integer.class);
        return value == null ? 1 : value - 1;
    }

    public List<String> categories() {
        return jdbcTemplate.query(
                "SELECT DISTINCT category FROM model_item ORDER BY category",
                (rs, rowNum) -> rs.getString("category")
        );
    }

    private static ModelRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ModelRow(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("cover_url"),
                rs.getString("file_url"),
                rs.getString("description"),
                rs.getString("tags"),
                rs.getString("status"),
                rs.getInt("sort_no"),
                rs.getTimestamp("updated_at").toLocalDateTime().toString()
        );
    }

    public record ModelRow(
            String id,
            String title,
            String category,
            String cover,
            String fileUrl,
            String desc,
            String tags,
            String status,
            int sortNo,
            String updatedAt
    ) {
    }

    public record ModelInput(
            String title,
            String category,
            String cover,
            String fileUrl,
            String desc,
            String tags,
            String status,
            int sortNo
    ) {
    }

    public record ModelQuery(
            String keyword,
            String category,
            boolean publishedOnly,
            int limit,
            int offset
    ) {
    }

    public record ModelPage(List<ModelRow> rows, long total) {
    }
}
