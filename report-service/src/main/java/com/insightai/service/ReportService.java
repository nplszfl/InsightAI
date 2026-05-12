package com.insightai.service;

import com.insightai.common.dto.ReportDto;
import com.insightai.common.model.Report;
import com.insightai.common.model.Visualization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Report Management Service
 * Handles report creation, update, and retrieval with visualizations
 */
@Slf4j
@Service
public class ReportService {

    private final ConcurrentHashMap<Long, Report> reportStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Create a new report
     */
    public ReportDto createReport(ReportDto dto) {
        log.info("Creating new report: {}", dto.getName());
        
        Long reportId = idGenerator.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        
        Report report = Report.builder()
                .id(reportId)
                .name(dto.getName())
                .type(dto.getType())
                .config(dto.getConfig())
                .filters(dto.getFilters())
                .createdBy(dto.getCreatedBy())
                .createdAt(now)
                .updatedAt(now)
                .status(1)
                .build();
        
        reportStore.put(reportId, report);
        
        return toDto(report, dto.getVisualizations());
    }

    /**
     * Get report by ID with visualizations
     */
    public Optional<ReportDto> getReportById(Long id) {
        log.info("Fetching report with ID: {}", id);
        Report report = reportStore.get(id);
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
        return reportStore.values().stream()
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Update an existing report
     */
    public Optional<ReportDto> updateReport(Long id, ReportDto dto) {
        log.info("Updating report with ID: {}", id);
        Report existing = reportStore.get(id);
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
        
        reportStore.put(id, existing);
        
        return Optional.of(toDto(existing, dto.getVisualizations()));
    }

    /**
     * Delete a report
     */
    public boolean deleteReport(Long id) {
        log.info("Deleting report with ID: {}", id);
        return reportStore.remove(id) != null;
    }

    /**
     * Get reports by type
     */
    public List<ReportDto> getReportsByType(String type) {
        log.info("Fetching reports of type: {}", type);
        return reportStore.values().stream()
                .filter(r -> type.equals(r.getType()))
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Get reports created by a specific user
     */
    public List<ReportDto> getReportsByUser(Long userId) {
        log.info("Fetching reports created by user: {}", userId);
        return reportStore.values().stream()
                .filter(r -> userId.equals(r.getCreatedBy()))
                .map(r -> toDto(r, List.of()))
                .collect(Collectors.toList());
    }

    /**
     * Add visualization to a report
     */
    public boolean addVisualizationToReport(Long reportId, ReportDto.VisualizationDto visualization) {
        log.info("Adding visualization to report: {}", reportId);
        Report report = reportStore.get(reportId);
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
        Report report = reportStore.get(reportId);
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