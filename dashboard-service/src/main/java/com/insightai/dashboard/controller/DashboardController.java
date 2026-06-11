package com.insightai.dashboard.controller;

import com.insightai.dashboard.dto.*;
import com.insightai.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Dashboard REST controller.
 * Exposes the core business operations: dashboards, widgets, snapshots, statistics.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ==================== Dashboards ====================

    @PostMapping
    public ResponseEntity<DashboardDto> create(@RequestBody CreateDashboardRequest request) {
        return ResponseEntity.ok(dashboardService.createDashboard(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DashboardDto> get(@PathVariable Long id) {
        return dashboardService.getDashboard(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DashboardDto> update(@PathVariable Long id,
                                               @RequestBody UpdateDashboardRequest request) {
        return ResponseEntity.ok(dashboardService.updateDashboard(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dashboardService.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DashboardDto>> list(
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String visibility) {
        return ResponseEntity.ok(dashboardService.listDashboards(ownerId, visibility));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<DashboardDto> changeVisibility(@PathVariable Long id,
                                                          @RequestParam String visibility) {
        return ResponseEntity.ok(dashboardService.changeVisibility(id, visibility));
    }

    // ==================== Widgets ====================

    @PostMapping("/{id}/widgets")
    public ResponseEntity<DashboardWidgetDto> addWidget(@PathVariable Long id,
                                                         @RequestBody AddWidgetRequest request) {
        return ResponseEntity.ok(dashboardService.addWidget(id, request));
    }

    @GetMapping("/{id}/widgets")
    public ResponseEntity<List<DashboardWidgetDto>> listWidgets(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.listWidgets(id));
    }

    @PatchMapping("/{dashboardId}/widgets/{widgetId}/move")
    public ResponseEntity<DashboardWidgetDto> moveWidget(@PathVariable Long dashboardId,
                                                          @PathVariable Long widgetId,
                                                          @RequestParam int x,
                                                          @RequestParam int y) {
        return ResponseEntity.ok(dashboardService.moveWidget(dashboardId, widgetId, x, y));
    }

    @DeleteMapping("/{dashboardId}/widgets/{widgetId}")
    public ResponseEntity<Void> removeWidget(@PathVariable Long dashboardId,
                                              @PathVariable Long widgetId) {
        dashboardService.removeWidget(dashboardId, widgetId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Snapshots ====================

    @PostMapping("/{id}/snapshots")
    public ResponseEntity<List<WidgetSnapshotDto>> captureSnapshot(@PathVariable Long id,
                                                                   @RequestParam(defaultValue = "manual") String triggeredBy) {
        return ResponseEntity.ok(dashboardService.captureSnapshot(id, triggeredBy));
    }

    @PostMapping("/{dashboardId}/widgets/{widgetId}/snapshots")
    public ResponseEntity<WidgetSnapshotDto> captureWidgetSnapshot(
            @PathVariable Long dashboardId,
            @PathVariable Long widgetId,
            @RequestBody Map<String, Object> payload,
            @RequestParam(defaultValue = "manual") String triggeredBy) {
        return ResponseEntity.ok(dashboardService.captureWidgetSnapshot(dashboardId, widgetId, payload, triggeredBy));
    }

    @GetMapping("/{id}/snapshots")
    public ResponseEntity<List<WidgetSnapshotDto>> listSnapshots(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.listSnapshots(id));
    }

    @GetMapping("/{dashboardId}/widgets/{widgetId}/snapshots")
    public ResponseEntity<List<WidgetSnapshotDto>> listWidgetSnapshots(
            @PathVariable Long dashboardId,
            @PathVariable Long widgetId) {
        return ResponseEntity.ok(dashboardService.listSnapshotsForWidget(dashboardId, widgetId));
    }

    // ==================== Statistics ====================

    @GetMapping("/{id}/statistics")
    public ResponseEntity<DashboardStatisticsDto> statistics(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getStatistics(id));
    }
}
