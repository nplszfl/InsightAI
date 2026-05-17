package com.insightai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.insightai.common.dto.ReportDto;
import com.insightai.common.model.Report;
import com.insightai.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Report Management Service
 * Handles report creation, update, and retrieval with MyBatis Plus persistence
 */
@Slf4j
@Service
public class ReportService extends ServiceImpl<ReportRepository, Report> {

    /**
     * Create a new report
     */
    public ReportDto createReport(ReportDto dto) {
        log.info("Creating new report: {}", dto.getName());

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
     * Update an existing report
     */
    public Optional<ReportDto> updateReport(Long id, ReportDto dto) {
        log.info("Updating report with ID: {}", id);
        Report existing = this.getById(id);
        if (existing == null) {
            return Optional.empty();
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
     * Delete a report
     */
    public boolean deleteReport(Long id) {
        log.info("Deleting report with ID: {}", id);
        return this.removeById(id);
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
