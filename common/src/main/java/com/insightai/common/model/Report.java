package com.insightai.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // DASHBOARD, SCHEDULED, AD_HOC
    private String config; // JSON config for report settings
    private String filters; // JSON for default filters
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private Integer status; // 1=active, 0=inactive
}
