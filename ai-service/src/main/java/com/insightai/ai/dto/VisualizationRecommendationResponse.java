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
public class VisualizationRecommendationResponse {
    private String recommendedChartType;
    private List<String> alternativeChartTypes;
    private String reasoning;
    private Map<String, String> axisMapping;
}