package com.insightai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.insightai.common.model.Visualization;
import com.insightai.repository.VisualizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Visualization Management Service
 * Handles chart/visualization creation and configuration with MyBatis Plus persistence
 */
@Slf4j
@Service
public class VisualizationService extends ServiceImpl<VisualizationRepository, Visualization> {

    /**
     * Create a new visualization
     */
    public Visualization createVisualization(Visualization visualization) {
        log.info("Creating new visualization for report: {}", visualization.getReportId());

        visualization.setCreatedAt(LocalDateTime.now());
        visualization.setUpdatedAt(LocalDateTime.now());

        this.save(visualization);
        return visualization;
    }

    /**
     * Get visualization by ID
     */
    public Optional<Visualization> getVisualizationById(Long id) {
        log.info("Fetching visualization with ID: {}", id);
        Visualization viz = this.getById(id);
        return Optional.ofNullable(viz);
    }

    /**
     * Get all visualizations for a report
     */
    public List<Visualization> getVisualizationsByReportId(Long reportId) {
        log.info("Fetching visualizations for report: {}", reportId);
        return this.lambdaQuery()
                .eq(Visualization::getReportId, reportId)
                .orderByAsc(Visualization::getPosition)
                .list();
    }

    /**
     * Update an existing visualization
     */
    public Optional<Visualization> updateVisualization(Long id, Visualization dto) {
        log.info("Updating visualization with ID: {}", id);
        Visualization existing = this.getById(id);
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

        this.updateById(existing);
        return Optional.of(existing);
    }

    /**
     * Delete a visualization
     */
    public boolean deleteVisualization(Long id) {
        log.info("Deleting visualization with ID: {}", id);
        return this.removeById(id);
    }

    /**
     * Delete all visualizations for a report
     */
    public void deleteVisualizationsByReportId(Long reportId) {
        log.info("Deleting all visualizations for report: {}", reportId);
        this.lambdaUpdate()
                .eq(Visualization::getReportId, reportId)
                .remove();
    }

    /**
     * Update visualization positions
     */
    public void updatePositions(Long reportId, List<Long> visualizationIds) {
        log.info("Updating positions for {} visualizations in report: {}", visualizationIds.size(), reportId);
        for (int i = 0; i < visualizationIds.size(); i++) {
            Long vizId = visualizationIds.get(i);
            Visualization viz = this.getById(vizId);
            if (viz != null && reportId.equals(viz.getReportId())) {
                viz.setPosition(i + 1);
                viz.setUpdatedAt(LocalDateTime.now());
                this.updateById(viz);
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
            Visualization viz = this.getById(vizId);
            if (viz != null && reportId.equals(viz.getReportId())) {
                viz.setPosition(i + 1);
                viz.setUpdatedAt(LocalDateTime.now());
                this.updateById(viz);
            } else {
                log.warn("Visualization {} not found or doesn't belong to report {}", vizId, reportId);
                return false;
            }
        }
        return true;
    }
}
