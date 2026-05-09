package com.insightai.ai.dto;

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
public class AttributionAnalysisResponse {
    private String metricName;
    private double changeAmount;
    private double changePercentage;
    private String direction;
    private List<AttributionFactor> attributedFactors;
    private String explanation;
    private List<String> recommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributionFactor {
        private String factor;
        private double contribution;
        private double percentageContribution;
        private String description;
    }
}