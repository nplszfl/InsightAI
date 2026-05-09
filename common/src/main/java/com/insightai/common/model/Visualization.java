package com.insightai.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("visualization")
public class Visualization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private String chartType; // TABLE, BAR, LINE, PIE, SCATTER, HEATMAP
    private String title;
    private String queryId; // Associated query
    private String config; // JSON for chart configuration
    private Integer position; // Order in dashboard
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
