package com.insightai.dashboard.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Request body for creating a new dashboard. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDashboardRequest {
    private String name;
    private String description;
    private String ownerId;
    private String visibility; // optional; defaults to PRIVATE
    private String category;   // optional; e.g. "sales", "ops", "finance"
}

/** Request body for partial dashboard updates. Null fields are preserved. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDashboardRequest {
    private String name;
    private String description;
    private String category;
}

/** Dashboard response. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private Long id;
    private String name;
    private String description;
    private String ownerId;
    private String visibility;
    private String category;
    private int widgetCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

/** Aggregate stats about a dashboard. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsDto {
    private Long dashboardId;
    private int widgetCount;
    private int totalArea;
    private LocalDateTime lastUpdatedAt;
    private List<String> chartTypes;
    private Map<String, Long> widgetCountByChartType;
}

/** Request to add a widget onto a dashboard at a given grid position. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddWidgetRequest {
    private String title;
    private String queryId;     // pointer to a saved query in query-service
    private String chartType;   // LINE, BAR, PIE, TABLE, KPI, AREA, SCATTER
    private int positionX;
    private int positionY;
    private int width;
    private int height;
    private Map<String, Object> config; // chart-specific options
}

/** Widget response. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardWidgetDto {
    private Long id;
    private Long dashboardId;
    private String title;
    private String queryId;
    private String chartType;
    private int positionX;
    private int positionY;
    private int width;
    private int height;
    private Map<String, Object> config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

/** A point-in-time rendered snapshot of one widget. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetSnapshotDto {
    private Long id;
    private Long dashboardId;
    private Long widgetId;
    private Map<String, Object> payload;
    private java.math.BigDecimal metricValue;
    private String triggeredBy; // "manual" | "scheduled" | "alert"
    private LocalDateTime capturedAt;
}
