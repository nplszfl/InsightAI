package com.insightai.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Widget snapshot. Point-in-time rendered data of a widget. Used for historical
 * comparison, alerts, and offline dashboard reload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("widget_snapshot")
public class WidgetSnapshot {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dashboardId;
    private Long widgetId;
    private String payload;       // JSON-serialized rendered data
    private BigDecimal metricValue; // primary KPI value, if applicable
    private String triggeredBy;   // manual, scheduled, alert

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime capturedAt;
}
