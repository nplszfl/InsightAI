package com.insightai.forecasting.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisRequest {
    @NotBlank(message = "Metric name is required")
    private String metricName;
    
    @NotNull(message = "Data points are required")
    @Size(min = 3, message = "At least 3 data points required")
    private List<DataPoint> dataPoints;
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String createdBy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDateTime timestamp;
        private Double value;
    }
}