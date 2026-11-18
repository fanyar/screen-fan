package com.fan.screenadmin.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ModelProgressRepository {

    private static final RowMapper<ProgressRow> ROW_MAPPER = ModelProgressRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ModelProgressRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ProgressRow> list(String modelId) {
        return jdbcTemplate.query(
                "SELECT * FROM model_progress_node WHERE model_id = ? ORDER BY sort_no, node_type, name",
                ROW_MAPPER,
                modelId
        );
    }

    public int countByModel(String modelId) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM model_progress_node WHERE model_id = ?",
                Integer.class,
                modelId
        );
        return value == null ? 0 : value;
    }

    public void replace(String modelId, List<ProgressInput> nodes) {
        jdbcTemplate.update("DELETE FROM model_progress_node WHERE model_id = ?", modelId);
        nodes.forEach(node -> insert(modelId, node));
    }

    public void deleteByModel(String modelId) {
        jdbcTemplate.update("DELETE FROM model_progress_node WHERE model_id = ?", modelId);
        jdbcTemplate.update("DELETE FROM model_progress_snapshot WHERE model_id = ?", modelId);
        jdbcTemplate.update("DELETE FROM model_progress_audit WHERE model_id = ?", modelId);
    }

    private void insert(String modelId, ProgressInput node) {
        jdbcTemplate.update(
                """
                        INSERT INTO model_progress_node(
                            id, model_id, parent_id, node_type, name, mesh_key, status,
                            progress_percent, weight, planned_start_date, planned_end_date,
                            actual_start_date, actual_end_date, owner, delay_reason, remark, sort_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                node.id(),
                modelId,
                node.parentId(),
                node.nodeType(),
                node.name(),
                node.meshKey(),
                node.status(),
                node.progressPercent(),
                node.weight(),
                node.plannedStartDate(),
                node.plannedEndDate(),
                node.actualStartDate(),
                node.actualEndDate(),
                node.owner(),
                node.delayReason(),
                node.remark(),
                node.sortNo()
        );
    }

    public void insertSnapshot(String modelId, List<ProgressRow> rows) {
        try {
            String json = objectMapper.writeValueAsString(rows);
            jdbcTemplate.update(
                    "INSERT INTO model_progress_snapshot(id, model_id, nodes_json) VALUES (?, ?, ?)",
                    UUID.randomUUID().toString(),
                    modelId,
                    json
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("进度快照序列化失败", exception);
        }
    }

    public List<ModelProgressDtos.SnapshotResponse> snapshots(String modelId) {
        return jdbcTemplate.query(
                "SELECT id, model_id, captured_at FROM model_progress_snapshot WHERE model_id = ? ORDER BY captured_at DESC",
                (rs, rowNum) -> new ModelProgressDtos.SnapshotResponse(
                        rs.getString("id"),
                        rs.getString("model_id"),
                        rs.getTimestamp("captured_at").toLocalDateTime().toString()
                ),
                modelId
        );
    }

    public void audit(String modelId, String action, String detail) {
        jdbcTemplate.update(
                "INSERT INTO model_progress_audit(id, model_id, action, detail) VALUES (?, ?, ?, ?)",
                UUID.randomUUID().toString(),
                modelId,
                action,
                detail
        );
    }

    public List<ModelProgressDtos.AuditResponse> audits(String modelId) {
        return jdbcTemplate.query(
                "SELECT id, action, created_at FROM model_progress_audit WHERE model_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> new ModelProgressDtos.AuditResponse(
                        rs.getString("id"),
                        rs.getString("action"),
                        rs.getTimestamp("created_at").toLocalDateTime().toString()
                ),
                modelId
        );
    }

    public Optional<String> latestSnapshotJson(String modelId) {
        return jdbcTemplate.query(
                "SELECT nodes_json FROM model_progress_snapshot WHERE model_id = ? ORDER BY captured_at DESC LIMIT 1",
                (rs, rowNum) -> rs.getString("nodes_json"),
                modelId
        ).stream().findFirst();
    }

    public Optional<LocalDateTime> latestUpdatedAt(String modelId) {
        return jdbcTemplate.query(
                "SELECT MAX(updated_at) FROM model_progress_node WHERE model_id = ?",
                (rs, rowNum) -> {
                    Timestamp value = rs.getTimestamp(1);
                    return value == null ? null : value.toLocalDateTime();
                },
                modelId
        ).stream().filter(value -> value != null).findFirst();
    }

    private static ProgressRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProgressRow(
                rs.getString("id"),
                rs.getString("parent_id"),
                rs.getString("node_type"),
                rs.getString("name"),
                rs.getString("mesh_key"),
                rs.getString("status"),
                rs.getInt("progress_percent"),
                rs.getBigDecimal("weight").doubleValue(),
                localDate(rs.getDate("planned_start_date")),
                localDate(rs.getDate("planned_end_date")),
                localDate(rs.getDate("actual_start_date")),
                localDate(rs.getDate("actual_end_date")),
                rs.getString("owner"),
                rs.getString("delay_reason"),
                rs.getString("remark"),
                rs.getInt("sort_no")
        );
    }

    private static String localDate(java.sql.Date value) {
        return value == null ? null : value.toString();
    }

    public record ProgressRow(
            String id,
            String parentId,
            String nodeType,
            String name,
            String meshKey,
            String status,
            int progressPercent,
            double weight,
            String plannedStartDate,
            String plannedEndDate,
            String actualStartDate,
            String actualEndDate,
            String owner,
            String delayReason,
            String remark,
            int sortNo
    ) {
    }

    public record ProgressInput(
            String id,
            String parentId,
            String nodeType,
            String name,
            String meshKey,
            String status,
            int progressPercent,
            double weight,
            String plannedStartDate,
            String plannedEndDate,
            String actualStartDate,
            String actualEndDate,
            String owner,
            String delayReason,
            String remark,
            int sortNo
    ) {
    }
}
