package com.insightai.controller;

import com.insightai.common.dto.ReportDto;
import com.insightai.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportDto> create(@RequestBody ReportDto dto) {
        return ResponseEntity.ok(reportService.createReport(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> getById(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ReportDto>> getAll() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportDto>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(reportService.getReportsByType(type));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportDto>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getReportsByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportDto> update(@PathVariable Long id, @RequestBody ReportDto dto) {
        return reportService.updateReport(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return reportService.deleteReport(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{reportId}/visualizations")
    public ResponseEntity<Void> addVisualization(
            @PathVariable Long reportId,
            @RequestBody ReportDto.VisualizationDto visualization) {
        return reportService.addVisualizationToReport(reportId, visualization)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{reportId}/visualizations/{vizId}")
    public ResponseEntity<Void> removeVisualization(
            @PathVariable Long reportId,
            @PathVariable Long vizId) {
        return reportService.removeVisualizationFromReport(reportId, vizId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReportDto>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(reportService.searchByName(name));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ReportDto> cloneReport(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return reportService.cloneReport(id, newName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishReport(@PathVariable Long id) {
        return reportService.publishReport(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishReport(@PathVariable Long id) {
        return reportService.unpublishReport(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<ReportDto>> getScheduledReports() {
        return ResponseEntity.ok(reportService.getScheduledReports());
    }

    @GetMapping("/stats/by-type")
    public ResponseEntity<Map<String, Long>> getCountByType() {
        return ResponseEntity.ok(reportService.countByType());
    }
}
