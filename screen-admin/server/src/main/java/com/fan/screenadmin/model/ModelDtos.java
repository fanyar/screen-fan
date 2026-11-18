package com.fan.screenadmin.model;

import java.util.List;

public final class ModelDtos {

    private ModelDtos() {
    }

    public record ModelResponse(
            String id,
            String title,
            String category,
            String cover,
            String fileUrl,
            String desc,
            List<String> tags,
            String status,
            String updatedAt
    ) {
    }

    public record ModelRequest(
            String title,
            String category,
            String cover,
            String fileUrl,
            String desc,
            List<String> tags,
            String status,
            Integer sortNo
    ) {
    }

    public record ModelPageResponse(
            List<ModelResponse> items,
            long total,
            int page,
            int pageSize
    ) {
    }

    public record ModelFacetResponse(List<String> categories) {
    }
}
