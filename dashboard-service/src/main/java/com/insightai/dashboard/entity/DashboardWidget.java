package com.insightai.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Dashboard widget entity. A widget is a single chart tile bound to a saved
 * query in query-service. It has a grid position and size.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dashboard_widget")
public class DashboardWidget {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dashboardId;
    private String title;
    private String queryId;
    private String chartType;  // LINE, BAR, PIE, TABLE, KPI, AREA, SCATTER
    private Integer positionX;
    private Integer positionY;
    private Integer width;
    private Integer height;
    private String config;     // JSON-serialized chart options

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
