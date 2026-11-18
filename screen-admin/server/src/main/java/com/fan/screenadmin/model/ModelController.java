package com.fan.screenadmin.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ModelController {

    private final ModelService modelService;
    private final ModelProgressService progressService;

    public ModelController(ModelService modelService, ModelProgressService progressService) {
        this.modelService = modelService;
        this.progressService = progressService;
    }

    @GetMapping("/api/models")
    public ModelDtos.ModelPageResponse list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "9") Integer pageSize
    ) {
        return modelService.list(keyword, category, page, pageSize, true);
    }

    @GetMapping("/api/models/facets")
    public ModelDtos.ModelFacetResponse facets() {
        return modelService.facets();
    }

    @GetMapping("/api/models/{id}")
    public ModelDtos.ModelResponse detail(@PathVariable String id) {
        return modelService.getPublished(id);
    }

    @GetMapping("/api/models/{id}/progress-tree")
    public ModelProgressDtos.TreeResponse progressTree(@PathVariable String id) {
        return progressService.tree(id, true);
    }

    @GetMapping("/api/models/{id}/progress-summary")
    public ModelProgressDtos.SummaryResponse progressSummary(@PathVariable String id) {
        return progressService.summary(id, true);
    }

    @GetMapping("/api/models/{id}/progress/snapshots")
    public List<ModelProgressDtos.SnapshotResponse> progressSnapshots(@PathVariable String id) {
        return progressService.snapshots(id, true);
    }

    @GetMapping("/api/admin/models")
    public List<ModelDtos.ModelResponse> adminList() {
        return modelService.listAll();
    }

    @PostMapping("/api/admin/models")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelDtos.ModelResponse create(@RequestBody ModelDtos.ModelRequest request) {
        return modelService.create(request);
    }

    @PutMapping("/api/admin/models/{id}")
    public ModelDtos.ModelResponse update(@PathVariable String id, @RequestBody ModelDtos.ModelRequest request) {
        return modelService.update(id, request);
    }

    @DeleteMapping("/api/admin/models/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        modelService.delete(id);
    }

    @GetMapping("/api/admin/models/{id}/progress-tree")
    public ModelProgressDtos.TreeResponse adminProgressTree(@PathVariable String id) {
        return progressService.tree(id, false);
    }

    @PutMapping("/api/admin/models/{id}/progress")
    public ModelProgressDtos.TreeResponse saveProgress(
            @PathVariable String id,
            @RequestBody ModelProgressDtos.SaveRequest request
    ) {
        return progressService.save(id, request);
    }

    @PostMapping(value = "/api/admin/models/{id}/progress/import", consumes = "text/csv")
    public ModelProgressDtos.TreeResponse importProgress(
            @PathVariable String id,
            @RequestBody String csv
    ) {
        return progressService.importCsv(id, csv);
    }

    @GetMapping("/api/admin/models/{id}/progress/audits")
    public List<ModelProgressDtos.AuditResponse> progressAudits(@PathVariable String id) {
        return progressService.audits(id);
    }
}
