package com.insightai.forecasting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("trend_analysis")
public class TrendAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String metricName;
    private String trendType;
    private BigDecimal slope;
    private BigDecimal intercept;
    private Double rSquared;
    private LocalDateTime analysisPeriodStart;
    private LocalDateTime analysisPeriodEnd;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}