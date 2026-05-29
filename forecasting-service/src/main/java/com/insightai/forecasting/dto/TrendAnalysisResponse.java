package com.insightai.forecasting.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisResponse {
    private Long id;
    private String metricName;
    private String trendType;
    private BigDecimal slope;
    private BigDecimal intercept;
    private Double rSquared;
    private LocalDateTime analysisPeriodStart;
    private LocalDateTime analysisPeriodEnd;
}