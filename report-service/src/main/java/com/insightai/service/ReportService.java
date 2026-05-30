package com.insightai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.insightai.common.dto.ReportDto;
import com.insightai.common.model.Report;
import com.insightai.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Report Management Service
 * Handles report creation, update, retrieval, and scheduling with MyBatis Plus persistence
 */
@Slf4j
@Service
public class ReportService extends ServiceImpl<ReportRepository, Report> {

    /**
     * Create a new report
     */
    public ReportDto createReport(ReportDto dto) {
        log.info("Creating new report: {}", dto.getName());

        // Validate
        validateReport(dto, null);

        LocalDateTime now = LocalDateTime.now();

        Report report = Report.builder()
                .name(dto.getName())
                .type(dto.getType())
                .config(dto.getConfig())
                .filters(dto.getFilters())
                .createdBy(dto.getCreatedBy())
                .createdAt(now)
                .updatedAt(now)
                .status(1)
                .build();

        this.save(report);
        log.info("Created report with ID: {}", report.getId());

        return toDto(report, dto.getVisualizations());
    }

    /**
     * Get report by ID with visualizations
     */
    public Optional<ReportDto> getReportById(Long id) {
        log.info("Fetching report with ID: {}", id);
        Report report = this.getById(id);
        if (report == null) {
            return Optional.empty();
        }
        return Optional.of(toDto(report, List.of()));
    }

    /**
     * Get all reports with pagination
     */
    public List<ReportDto> getAllReports() {
        log.info("Fetching all reports");
        return this.lambdaQuery()
                .orderByDesc(Report::getCreatedAt)
                .list()
                .stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Get active reports only
     */
    public List<ReportDto> getActiveReports() {
        log.info("Fetching active reports");
        return this.lambdaQuery()
                .eq(Report::getStatus, 1)
                .orderByDesc(Report::getCreatedAt)
                .list()
                .stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Update an existing report
     */
    public Optional<ReportDto> updateReport(Long id, ReportDto dto) {
        log.info("Updating report with ID: {}", id);
        Report existing = this.getById(id);
        if (existing == null) {
            return Optional.empty();
        }

        // Validate if name is being changed
        if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
            validateUniqueName(dto.getName(), id);
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getType() != null) {
            existing.setType(dto.getType());
        }
        if (dto.getConfig() != null) {
            existing.setConfig(dto.getConfig());
        }
        if (dto.getFilters() != null) {
            existing.setFilters(dto.getFilters());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return Optional.of(toDto(existing, dto.getVisualizations()));
    }

    /**
     * Delete a report (soft delete)
     */
    public boolean deleteReport(Long id) {
        log.info("Deleting report with ID: {}", id);
        Report report = this.getById(id);
        if (report == null) {
            return false;
        }
        report.setStatus(0);
        report.setUpdatedAt(LocalDateTime.now());
        return this.updateById(report);
    }

    /**
     * Get reports by type
     */
    public List<ReportDto> getReportsByType(String type) {
        log.info("Fetching reports of type: {}", type);
        return this.lambdaQuery()
                .eq(Report::getType, type)
                .orderByDesc(Report::getCreatedAt)
                .list()
                .stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Get reports created by a specific user
     */
    public List<ReportDto> getReportsByUser(Long userId) {
        log.info("Fetching reports created by user: {}", userId);
        return this.lambdaQuery()
                .eq(Report::getCreatedBy, userId)
                .orderByDesc(Report::getCreatedAt)
                .list()
                .stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Add visualization to a report
     */
    public boolean addVisualizationToReport(Long reportId, ReportDto.VisualizationDto visualization) {
        log.info("Adding visualization to report: {}", reportId);
        Report report = this.getById(reportId);
        if (report == null) {
            return false;
        }
        // In a real implementation, this would save to a visualization repository
        return true;
    }

    /**
     * Remove visualization from a report
     */
    public boolean removeVisualizationFromReport(Long reportId, Long visualizationId) {
        log.info("Removing visualization {} from report: {}", visualizationId, reportId);
        Report report = this.getById(reportId);
        if (report == null) {
            return false;
        }
        // In a real implementation, this would remove from a visualization repository
        return true;
    }

    /**
     * Enable a report
     */
    public boolean enableReport(Long id) {
        log.info("Enabling report: {}", id);
        Report report = this.getById(id);
        if (report == null) {
            return false;
        }
        report.setStatus(1);
        report.setUpdatedAt(LocalDateTime.now());
        return this.updateById(report);
    }

    /**
     * Disable a report
     */
    public boolean disableReport(Long id) {
        log.info("Disabling report: {}", id);
        Report report = this.getById(id);
        if (report == null) {
            return false;
        }
        report.setStatus(0);
        report.setUpdatedAt(LocalDateTime.now());
        return this.updateById(report);
    }

    /**
     * Get report statistics
     */
    public Map<String, Object> getStatistics() {
        long total = this.count();
        long active = this.count(this.lambdaQuery().eq(Report::getStatus, 1));
        long inactive = total - active;

        // Count by type
        Map<String, Long> byType = new HashMap<>();
        List<Report> allReports = this.list();
        for (Report r : allReports) {
            String type = r.getType() != null ? r.getType() : "UNKNOWN";
            byType.put(type, byType.getOrDefault(type, 0L) + 1);
        }

        return Map.of(
                "total", total,
                "active", active,
                "inactive", inactive,
                "countByType", byType
        );
    }

    /**
     * Search reports by name (partial match)
     */
    public List<ReportDto> searchByName(String name) {
        log.info("Searching reports by name: {}", name);
        return this.lambdaQuery()
                .like(Report::getName, name)
                .orderByDesc(Report::getCreatedAt)
                .list()
                .stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Clone/duplicate a report with a new name
     */
    public Optional<ReportDto> cloneReport(Long id, String newName) {
        log.info("Cloning report: {} as {}", id, newName);
        Report original = this.getById(id);
        if (original == null) {
            return Optional.empty();
        }

        validateUniqueName(newName, null);

        Report clone = Report.builder()
                .name(newName)
                .type(original.getType())
                .config(original.getConfig())
                .filters(original.getFilters())
                .createdBy(original.getCreatedBy())
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        this.save(clone);
        log.info("Cloned report with new ID: {}", clone.getId());
        return Optional.of(toDto(clone, List.of()));
    }

    /**
     * Publish a report (make it publicly available)
     */
    public boolean publishReport(Long id) {
        log.info("Publishing report: {}", id);
        Report report = this.getById(id);
        if (report == null) {
            return false;
        }
        report.setStatus(1);
        report.setUpdatedAt(LocalDateTime.now());
        return this.updateById(report);
    }

    /**
     * Unpublish a report (make it private)
     */
    public boolean unpublishReport(Long id) {
        log.info("Unpublishing report: {}", id);
        Report report = this.getById(id);
        if (report == null) {
            return false;
        }
        report.setStatus(0);
        report.setUpdatedAt(LocalDateTime.now());
        return this.updateById(report);
    }

    /**
     * Get scheduled reports (reports ready to be executed)
     */
    public List<ReportDto> getScheduledReports() {
        log.info("Fetching scheduled reports");
        return this.lambdaQuery()
                .eq(Report::getStatus, 1)
                .eq(Report::getType, "SCHEDULED")
                .orderByDesc(Report::getCreatedAt)
                .list()
                .stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Count reports by type
     */
    public Map<String, Long> countByType() {
        Map<String, Long> byType = new HashMap<>();
        List<Report> allReports = this.list();
        for (Report r : allReports) {
            String type = r.getType() != null ? r.getType() : "UNKNOWN";
            byType.put(type, byType.getOrDefault(type, 0L) + 1);
        }
        return byType;
    }

    /**
     * Validate report data
     */
    public void validateReport(ReportDto dto, Long excludeId) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Report name is required");
        }
        if (dto.getType() != null && !isValidReportType(dto.getType())) {
            throw new IllegalArgumentException("Invalid report type. Must be DASHBOARD, SCHEDULED, or AD_HOC");
        }
        validateUniqueName(dto.getName(), excludeId);
    }

    /**
     * Check if report type is valid
     */
    private boolean isValidReportType(String type) {
        return "DASHBOARD".equals(type) || "SCHEDULED".equals(type) || "AD_HOC".equals(type);
    }

    /**
     * Validate unique report name
     */
    private void validateUniqueName(String name, Long excludeId) {
        var query = this.lambdaQuery().eq(Report::getName, name);
        if (excludeId != null) {
            query.ne(Report::getId, excludeId);
        }
        if (query.count() > 0) {
            throw new IllegalArgumentException("Report with name '" + name + "' already exists");
        }
    }

    private ReportDto toDto(Report entity, List<ReportDto.VisualizationDto> visualizations) {
        return ReportDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .config(entity.getConfig())
                .filters(entity.getFilters())
                .createdBy(entity.getCreatedBy())
                .visualizations(visualizations)
                .build();
    }
}