package com.insightai.forecasting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("forecast_record")
public class ForecastRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String metricName;
    private LocalDateTime timePoint;
    private BigDecimal predictedValue;
    private BigDecimal actualValue;
    private BigDecimal confidenceInterval;
    private String forecastPeriod;
    private String trendDirection;
    private Double seasonalityScore;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}