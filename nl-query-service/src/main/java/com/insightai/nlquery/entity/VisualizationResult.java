package com.insightai.nlquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("visualization_result")
public class VisualizationResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long queryHistoryId;
    private String chartType;
    private String chartConfig;
    private String dataSummary;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}