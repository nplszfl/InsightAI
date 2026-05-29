package com.insightai.forecasting.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {
    private Long id;
    private String metricName;
    private LocalDateTime forecastTime;
    private BigDecimal predictedValue;
    private BigDecimal confidenceInterval;
    private String trendDirection;
    private Double seasonalityScore;
}