package com.insightai.common.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Aggregate view of a report's visualizations arranged for dashboard rendering.
 *
 * <p>Items are pre-sorted by position so the UI can render them top-to-bottom
 * without further client-side sorting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardLayoutDto {
    /** Report the layout belongs to. */
    private Long reportId;

    /** Total number of visualizations in the layout. */
    private int totalCount;

    /** Map of chart type → count, useful for the "you have 5 bar charts" summary tile. */
    private Map<String, Long> chartTypeCounts;

    /** Visualizations ordered by {@code position} ascending. */
    private List<VisualizationDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualizationDto {
        private Long id;
        private String chartType;
        private String title;
        private String queryId;
        private String config;
        private Integer position;
    }
}
