package com.insightai.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesForecastResponse {
    private List<ForecastPoint> forecast;
    private double trendPercentage;
    private String trendDirection;
    private String seasonality;
    private String confidence;
    private String modelUsed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPoint {
        private LocalDateTime timestamp;
        private double predictedValue;
        private double lowerBound;
        private double upperBound;
    }
}