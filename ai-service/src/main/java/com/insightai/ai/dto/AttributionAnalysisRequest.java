package com.insightai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributionAnalysisRequest {
    @NotBlank(message = "Metric name cannot be empty")
    private String metricName;
    private double currentValue;
    private double previousValue;
    private List<Map<String, Object>> contributingFactors;
    private String timeRange;
    private String analysisType;

    public enum AnalysisType {
        COMPARATIVE,
        TREND,
        SEASONAL,
        EVENT_BASED
    }
}