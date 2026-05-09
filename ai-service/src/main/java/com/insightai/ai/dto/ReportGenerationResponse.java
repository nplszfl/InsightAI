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
public class ReportGenerationResponse {
    private String title;
    private String content;
    private String executiveSummary;
    private List<String> keyInsights;
    private List<String> recommendations;
    private Map<String, String> metricsHighlighted;
    private String format;
}