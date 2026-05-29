package com.insightai.forecasting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("seasonality_detection")
public class SeasonalityDetection {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String metricName;
    private String seasonalityPattern;
    private Double strength;
    private Integer periodDays;
    private String peakTime;
    private String troughTime;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}