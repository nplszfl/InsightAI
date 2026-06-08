package com.insightai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.common.dto.DashboardLayoutDto;
import com.insightai.common.model.Visualization;
import com.insightai.repository.VisualizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // ==================== Business functions: validation, export, dashboard ====================

    /** Catalogue of supported chart types. Keep in sync with the UI chart picker. */
    public static final List<String> SUPPORTED_CHART_TYPES = List.of(
            "TABLE", "BAR", "LINE", "PIE", "SCATTER", "HEATMAP", "AREA", "BOX", "HISTOGRAM"
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Check if the given chart type is one of the supported values.
     * <p>Case-sensitive on purpose: callers should send upper-case values.
     */
    public boolean isValidChartType(String chartType) {
        return chartType != null && SUPPORTED_CHART_TYPES.contains(chartType);
    }

    /**
     * Validate a visualization before persisting it.
     *
     * <p>Enforces the minimum data-quality contract:
     * <ul>
     *   <li>{@code chartType} must be a supported value (see {@link #SUPPORTED_CHART_TYPES})</li>
     *   <li>{@code title} must be non-blank</li>
     *   <li>{@code position}, when present, must be non-negative</li>
     * </ul>
     *
     * @throws IllegalArgumentException when the visualization is invalid
     */
    public void validateVisualization(Visualization viz) {
        if (viz == null) {
            throw new IllegalArgumentException("Visualization must not be null");
        }
        if (!isValidChartType(viz.getChartType())) {
            throw new IllegalArgumentException(
                    "Invalid chart type '" + viz.getChartType() + "'. Must be one of: " + SUPPORTED_CHART_TYPES);
        }
        if (viz.getTitle() == null || viz.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (viz.getPosition() != null && viz.getPosition() < 0) {
            throw new IllegalArgumentException("Position must be non-negative, got: " + viz.getPosition());
        }
        // When config is set it must be valid JSON with at least an x and y field
        if (viz.getConfig() != null && !viz.getConfig().isBlank()) {
            validateConfig(viz.getConfig());
        }
    }

    /**
     * Parse and validate a JSON chart-config payload.
     * <p>The minimum contract is: object with {@code xField} and {@code yField} keys
     * — every chart needs at least one dimension and one measure.
     *
     * @return the parsed map for fluent re-use by callers
     * @throws IllegalArgumentException if the JSON is malformed or missing required keys
     */
    public Map<String, Object> validateConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            throw new IllegalArgumentException("Config must not be blank");
        }
        Map<String, Object> parsed;
        try {
            parsed = MAPPER.readValue(configJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Config is not valid JSON: " + e.getMessage(), e);
        }
        if (parsed == null) {
            throw new IllegalArgumentException("Config is empty");
        }
        if (!parsed.containsKey("xField") || String.valueOf(parsed.get("xField")).isBlank()) {
            throw new IllegalArgumentException("Config is missing required 'xField'");
        }
        if (!parsed.containsKey("yField") || String.valueOf(parsed.get("yField")).isBlank()) {
            throw new IllegalArgumentException("Config is missing required 'yField'");
        }
        return parsed;
    }

    /**
     * Render a tabular dataset as CSV text.
     *
     * <p>This is a deliberately small, dependency-free implementation:
     * <ul>
     *   <li>Columns are emitted in the order given by {@code headers}</li>
     *   <li>Values containing commas or quotes are quoted</li>
     *   <li>Inner quotes are doubled (RFC 4180 escaping)</li>
     *   <li>Lines end with a single {@code \n}</li>
     * </ul>
     *
     * @throws IllegalArgumentException if a row's column count doesn't match the headers
     */
    public String exportToCsv(List<String> headers, List<List<Object>> rows) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("CSV export requires at least one header column");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append('\n');
        if (rows != null) {
            for (List<Object> row : rows) {
                if (row == null) {
                    throw new IllegalArgumentException("CSV row must not be null");
                }
                if (row.size() != headers.size()) {
                    throw new IllegalArgumentException(
                            "CSV row column count (" + row.size() + ") does not match header count ("
                                    + headers.size() + ")");
                }
                for (int i = 0; i < row.size(); i++) {
                    if (i > 0) sb.append(',');
                    sb.append(csvEscape(row.get(i)));
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Build a complete dashboard layout for a given report — pre-sorted by
     * position and with a per-chart-type count summary.
     */
    public DashboardLayoutDto getDashboardLayout(Long reportId) {
        log.info("Building dashboard layout for report: {}", reportId);
        DashboardLayoutDto layout = DashboardLayoutDto.builder()
                .reportId(reportId)
                .build();

        List<Visualization> all = this.lambdaQuery()
                .eq(Visualization::getReportId, reportId)
                .list();
        // MySQL can return rows in any order; ensure layout is stable.
        all.sort(Comparator.comparing(
                v -> v.getPosition() == null ? Integer.MAX_VALUE : v.getPosition()));

        Map<String, Long> chartTypeCounts = new LinkedHashMap<>();
        List<DashboardLayoutDto.VisualizationDto> items = new ArrayList<>(all.size());
        for (Visualization v : all) {
            String type = v.getChartType() == null ? "UNKNOWN" : v.getChartType();
            chartTypeCounts.merge(type, 1L, Long::sum);
            items.add(DashboardLayoutDto.VisualizationDto.builder()
                    .id(v.getId())
                    .chartType(v.getChartType())
                    .title(v.getTitle())
                    .queryId(v.getQueryId())
                    .config(v.getConfig())
                    .position(v.getPosition())
                    .build());
        }

        layout.setTotalCount(items.size());
        layout.setChartTypeCounts(chartTypeCounts);
        layout.setItems(items);
        return layout;
    }

    /**
     * Clone a set of visualizations from one report to another, preserving
     * chart type, title, config and query, but resetting IDs and timestamps.
     *
     * @return the newly created visualization records
     */
    public List<Visualization> cloneVisualizationsToReport(Long sourceReportId, Long targetReportId,
                                                            List<Long> visualizationIds) {
        log.info("Cloning {} visualizations from report {} to {}",
                visualizationIds == null ? 0 : visualizationIds.size(), sourceReportId, targetReportId);
        if (visualizationIds == null || visualizationIds.isEmpty()) {
            return new ArrayList<>();
        }
        LocalDateTime now = LocalDateTime.now();
        List<Visualization> created = new ArrayList<>();
        for (Long vizId : visualizationIds) {
            Visualization original = this.getById(vizId);
            if (original == null) {
                log.warn("Skipping clone: visualization {} not found", vizId);
                continue;
            }
            if (!sourceReportId.equals(original.getReportId())) {
                log.warn("Skipping clone: visualization {} does not belong to report {}", vizId, sourceReportId);
                continue;
            }
            Visualization copy = Visualization.builder()
                    .reportId(targetReportId)
                    .chartType(original.getChartType())
                    .title(original.getTitle())
                    .queryId(original.getQueryId())
                    .config(original.getConfig())
                    .position(original.getPosition())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            this.save(copy);
            created.add(copy);
        }
        return created;
    }

    // ==================== Helpers ====================

    private String csvEscape(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        boolean needsQuoting = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needsQuoting) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
