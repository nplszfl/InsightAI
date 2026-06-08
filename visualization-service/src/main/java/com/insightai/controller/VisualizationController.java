package com.insightai.controller;

import com.insightai.common.dto.DashboardLayoutDto;
import com.insightai.common.model.Visualization;
import com.insightai.service.VisualizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visualizations")
@RequiredArgsConstructor
public class VisualizationController {

    private final VisualizationService visualizationService;

    @PostMapping
    public ResponseEntity<Visualization> create(@RequestBody Visualization visualization) {
        // Validate before persisting — fail fast with a 400 on bad input.
        visualizationService.validateVisualization(visualization);
        return ResponseEntity.ok(visualizationService.createVisualization(visualization));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visualization> getById(@PathVariable Long id) {
        return visualizationService.getVisualizationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<Visualization>> getByReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(visualizationService.getVisualizationsByReportId(reportId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visualization> update(
            @PathVariable Long id,
            @RequestBody Visualization visualization) {
        // Re-validate the incoming payload when it includes editable fields.
        if (visualization.getChartType() != null || visualization.getTitle() != null) {
            visualizationService.validateVisualization(visualization);
        }
        return visualizationService.updateVisualization(id, visualization)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return visualizationService.deleteVisualization(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/report/{reportId}")
    public ResponseEntity<Void> deleteByReport(@PathVariable Long reportId) {
        visualizationService.deleteVisualizationsByReportId(reportId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/report/{reportId}/positions")
    public ResponseEntity<Void> updatePositions(
            @PathVariable Long reportId,
            @RequestBody List<Long> visualizationIds) {
        visualizationService.updatePositions(reportId, visualizationIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reorder/{reportId}")
    public ResponseEntity<Void> reorder(
            @PathVariable Long reportId,
            @RequestBody List<Long> orderedVizIds) {
        return visualizationService.reorderVisualizations(reportId, orderedVizIds)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @GetMapping("/chart-types")
    public ResponseEntity<List<String>> getChartTypes() {
        return ResponseEntity.ok(visualizationService.getAvailableChartTypes());
    }

    /**
     * Build a complete dashboard layout for a report — pre-sorted by position.
     */
    @GetMapping("/layout/{reportId}")
    public ResponseEntity<DashboardLayoutDto> getDashboardLayout(@PathVariable Long reportId) {
        return ResponseEntity.ok(visualizationService.getDashboardLayout(reportId));
    }

    /**
     * Clone visualizations from one report to another. Body: { "sourceReportId": 1,
     * "targetReportId": 2, "visualizationIds": [10, 11, 12] }.
     */
    @PostMapping("/clone")
    public ResponseEntity<List<Visualization>> cloneVisualizations(@RequestBody CloneRequest body) {
        if (body == null || body.sourceReportId == null || body.targetReportId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Visualization> created = visualizationService.cloneVisualizationsToReport(
                body.sourceReportId, body.targetReportId, body.visualizationIds);
        return ResponseEntity.ok(created);
    }

    /**
     * Render a tabular dataset as CSV. Body: { "headers": ["a","b"], "rows": [[1,2]] }.
     */
    @PostMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(@RequestBody CsvExportRequest body) {
        String csv = visualizationService.exportToCsv(body.headers, body.rows);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header("Content-Disposition", "attachment; filename=export.csv")
                .body(csv);
    }

    // ---- Inline request DTOs (kept small and local) ----

    @lombok.Data
    public static class CloneRequest {
        private Long sourceReportId;
        private Long targetReportId;
        private List<Long> visualizationIds;
    }

    @lombok.Data
    public static class CsvExportRequest {
        private List<String> headers;
        private List<List<Object>> rows;
    }
}
