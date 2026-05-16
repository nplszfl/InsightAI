package com.insightai.controller;

import com.insightai.common.model.Visualization;
import com.insightai.service.VisualizationService;
import lombok.RequiredArgsConstructor;
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
}
