package com.insightai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationRequest {
    @NotBlank(message = "Topic cannot be empty")
    private String topic;
    private String dataSummary;
    private String metrics;
    private String timeRange;
    private String audience;
    private ReportFormat format;

    public enum ReportFormat {
        EXECUTIVE_SUMMARY,
        DETAILED_ANALYSIS,
        TREND_REPORT
    }
}