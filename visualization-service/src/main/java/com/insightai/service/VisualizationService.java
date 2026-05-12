package com.insightai.service;

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
 * Visualization Management Service
 * Handles chart/visualization creation and configuration
 */
@Slf4j
@Service
public class VisualizationService {

    private final ConcurrentHashMap<Long, Visualization> visualizationStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Create a new visualization
     */
    public Visualization createVisualization(Visualization visualization) {
        log.info("Creating new visualization for report: {}", visualization.getReportId());
        
        Long vizId = idGenerator.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        
        visualization.setId(vizId);
        visualization.setCreatedAt(now);
        visualization.setUpdatedAt(now);
        
        visualizationStore.put(vizId, visualization);
        
        return visualization;
    }

    /**
     * Get visualization by ID
     */
    public Optional<Visualization> getVisualizationById(Long id) {
        log.info("Fetching visualization with ID: {}", id);
        return Optional.ofNullable(visualizationStore.get(id));
    }

    /**
     * Get all visualizations for a report
     */
    public List<Visualization> getVisualizationsByReportId(Long reportId) {
        log.info("Fetching visualizations for report: {}", reportId);
        return visualizationStore.values().stream()
                .filter(v -> reportId.equals(v.getReportId()))
                .sorted((v1, v2) -> {
                    if (v1.getPosition() == null && v2.getPosition() == null) return 0;
                    if (v1.getPosition() == null) return 1;
                    if (v2.getPosition() == null) return -1;
                    return v1.getPosition().compareTo(v2.getPosition());
                })
                .collect(Collectors.toList());
    }

    /**
     * Update an existing visualization
     */
    public Optional<Visualization> updateVisualization(Long id, Visualization dto) {
        log.info("Updating visualization with ID: {}", id);
        Visualization existing = visualizationStore.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        
        if (dto.getChartType() != null) {
            existing.setChartType(dto.getChartType());
        }
        if (dto.getTitle() != null) {
            existing.setTitle(dto.getTitle());
        }
        if (dto.getConfig() != null) {
            existing.setConfig(dto.getConfig());
        }
        if (dto.getPosition() != null) {
            existing.setPosition(dto.getPosition());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        
        visualizationStore.put(id, existing);
        
        return Optional.of(existing);
    }

    /**
     * Delete a visualization
     */
    public boolean deleteVisualization(Long id) {
        log.info("Deleting visualization with ID: {}", id);
        return visualizationStore.remove(id) != null;
    }

    /**
     * Delete all visualizations for a report
     */
    public void deleteVisualizationsByReportId(Long reportId) {
        log.info("Deleting all visualizations for report: {}", reportId);
        visualizationStore.entrySet().removeIf(entry -> reportId.equals(entry.getValue().getReportId()));
    }

    /**
     * Update visualization positions
     */
    public void updatePositions(Long reportId, List<Long> visualizationIds) {
        log.info("Updating positions for {} visualizations in report: {}", visualizationIds.size(), reportId);
        for (int i = 0; i < visualizationIds.size(); i++) {
            Visualization viz = visualizationStore.get(visualizationIds.get(i));
            if (viz != null && reportId.equals(viz.getReportId())) {
                viz.setPosition(i + 1);
                viz.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    /**
     * Get available chart types
     */
    public List<String> getAvailableChartTypes() {
        return List.of("TABLE", "BAR", "LINE", "PIE", "SCATTER", "HEATMAP", "AREA", "BOX", "HISTOGRAM");
    }

    /**
     * Reorder visualizations within a report
     */
    public boolean reorderVisualizations(Long reportId, List<Long> orderedVizIds) {
        log.info("Reordering {} visualizations in report: {}", orderedVizIds.size(), reportId);
        
        for (int i = 0; i < orderedVizIds.size(); i++) {
            Long vizId = orderedVizIds.get(i);
            Visualization viz = visualizationStore.get(vizId);
            if (viz != null && reportId.equals(viz.getReportId())) {
                viz.setPosition(i + 1);
                viz.setUpdatedAt(LocalDateTime.now());
            } else {
                log.warn("Visualization {} not found or doesn't belong to report {}", vizId, reportId);
                return false;
            }
        }
        return true;
    }
}