package com.fan.screenadmin.model;

import java.util.List;

public final class ModelProgressDtos {

    private ModelProgressDtos() {
    }

    public record NodeResponse(
            String id,
            String parentId,
            String nodeType,
            String name,
            String meshKey,
            String status,
            String displayStatus,
            Integer progressPercent,
            Double weight,
            String plannedStartDate,
            String plannedEndDate,
            String actualStartDate,
            String actualEndDate,
            String owner,
            String delayReason,
            String remark,
            List<NodeResponse> children
    ) {
    }

    public record SummaryResponse(
            int total,
            int completed,
            int inProgress,
            int notStarted,
            int delayed,
            int progressPercent,
            String currentStage,
            String updatedAt
    ) {
    }

    public record TreeResponse(
            List<NodeResponse> nodes,
            SummaryResponse summary,
            List<String> unboundNodeNames
    ) {
    }

    public record NodeRequest(
            String id,
            String parentId,
            String nodeType,
            String name,
            String meshKey,
            String status,
            Integer progressPercent,
            Double weight,
            String plannedStartDate,
            String plannedEndDate,
            String actualStartDate,
            String actualEndDate,
            String owner,
            String delayReason,
            String remark,
            Integer sortNo
    ) {
    }

    public record SaveRequest(List<NodeRequest> nodes) {
    }

    public record SnapshotResponse(String id, String modelId, String capturedAt) {
    }

    public record AuditResponse(String id, String action, String createdAt) {
    }
}
