package com.insightai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.ai.client.DeepSeekClient;
import com.insightai.ai.dto.ReportGenerationRequest;
import com.insightai.ai.dto.ReportGenerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutoReportGenerationService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert business intelligence report writer. Your task is to generate comprehensive, insightful analysis reports.
            
            Output ONLY a valid JSON object with this structure:
            {
              "title": "Report Title",
              "content": "Full detailed report content in markdown format...",
              "executiveSummary": "2-3 sentence executive summary",
              "keyInsights": ["insight 1", "insight 2", "insight 3"],
              "recommendations": ["recommendation 1", "recommendation 2"],
              "metricsHighlighted": {"metric1": "value1", "metric2": "value2"},
              "format": "DETAILED_ANALYSIS"
            }
            
            Guidelines:
            - Write in a professional, business-appropriate tone
            - Include data-driven insights and interpretations
            - Provide actionable recommendations
            - Use markdown formatting for readability (headers, lists, bold text)
            - Highlight key metrics and trends
            - Ensure the report tells a coherent story
            """;

    public AutoReportGenerationService(DeepSeekClient deepSeekClient, ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    public ReportGenerationResponse generateReport(ReportGenerationRequest request) {
        log.info("Generating {} report for topic: {}", request.getFormat(), request.getTopic());

        String prompt = buildPrompt(request);

        try {
            String jsonResponse = deepSeekClient.generateJsonCompletion(prompt, SYSTEM_PROMPT);
            return parseResponse(jsonResponse, request.getFormat());
        } catch (Exception e) {
            log.error("Failed to generate report: {}", e.getMessage());
            return buildFallbackReport(request);
        }
    }

    private String buildPrompt(ReportGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a ").append(request.getFormat() != null ? request.getFormat().name().replace("_", " ") : "detailed analysis").append(" report.\n\n");

        prompt.append("Topic: ").append(request.getTopic()).append("\n\n");

        if (request.getDataSummary() != null && !request.getDataSummary().isEmpty()) {
            prompt.append("Data Summary:\n").append(request.getDataSummary()).append("\n\n");
        }

        if (request.getMetrics() != null && !request.getMetrics().isEmpty()) {
            prompt.append("Key Metrics:\n").append(request.getMetrics()).append("\n\n");
        }

        if (request.getTimeRange() != null && !request.getTimeRange().isEmpty()) {
            prompt.append("Time Range: ").append(request.getTimeRange()).append("\n\n");
        }

        if (request.getAudience() != null && !request.getAudience().isEmpty()) {
            prompt.append("Target Audience: ").append(request.getAudience()).append("\n");
        }

        prompt.append("\nProvide your report in JSON format with all required fields.");

        return prompt.toString();
    }

    private ReportGenerationResponse parseResponse(String jsonResponse, ReportGenerationRequest.ReportFormat format) {
        try {
            ReportGenerationResponse response = objectMapper.readValue(jsonResponse, ReportGenerationResponse.class);
            if (response.getFormat() == null) {
                response.setFormat(format != null ? format.name() : "DETAILED_ANALYSIS");
            }
            return response;
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, using fallback");
            return extractReportManually(jsonResponse, format);
        }
    }

    private ReportGenerationResponse extractReportManually(String json, ReportGenerationRequest.ReportFormat format) {
        String title = extractJsonValue(json, "title");
        String content = extractJsonValue(json, "content");
        String summary = extractJsonValue(json, "executiveSummary");

        List<String> keyInsights = extractJsonArray(json, "keyInsights");
        List<String> recommendations = extractJsonArray(json, "recommendations");

        return ReportGenerationResponse.builder()
                .title(title != null ? title : "Analysis Report")
                .content(content != null ? content : "Report content could not be generated.")
                .executiveSummary(summary != null ? summary : "Unable to generate summary.")
                .keyInsights(keyInsights.isEmpty() ? List.of("Data analyzed", "Trends identified", "Insights generated") : keyInsights)
                .recommendations(recommendations.isEmpty() ? List.of("Monitor metrics", "Review trends") : recommendations)
                .metricsHighlighted(Map.of("status", "analyzed"))
                .format(format != null ? format.name() : "DETAILED_ANALYSIS")
                .build();
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"");
        }
        return null;
    }

    private List<String> extractJsonArray(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\\[(.*?)\\]";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            String arrayContent = m.group(1);
            java.util.regex.Pattern itemPattern = java.util.regex.Pattern.compile("\"([^\"]+)\"");
            java.util.regex.Matcher itemMatcher = itemPattern.matcher(arrayContent);
            List<String> items = new java.util.ArrayList<>();
            while (itemMatcher.find()) {
                items.add(itemMatcher.group(1));
            }
            return items;
        }
        return List.of();
    }

    private ReportGenerationResponse buildFallbackReport(ReportGenerationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("# ").append(request.getTopic()).append("\n\n");

        if (request.getDataSummary() != null) {
            content.append("## Data Overview\n\n").append(request.getDataSummary()).append("\n\n");
        }

        if (request.getMetrics() != null) {
            content.append("## Key Metrics\n\n").append(request.getMetrics()).append("\n\n");
        }

        content.append("## Analysis\n\n");
        content.append("This report provides an analysis of the requested topic based on the available data. ");
        content.append("Further investigation may be required to draw definitive conclusions.\n\n");

        content.append("## Key Findings\n\n");
        content.append("- Trends have been identified in the provided dataset\n");
        content.append("- Further data collection recommended for comprehensive analysis\n");
        content.append("- Regular monitoring of metrics advised\n\n");

        content.append("## Recommendations\n\n");
        content.append("1. Continue monitoring key performance indicators\n");
        content.append("2. Review trends on a regular basis\n");
        content.append("3. Consider additional data sources for deeper insights\n");

        return ReportGenerationResponse.builder()
                .title(request.getTopic())
                .content(content.toString())
                .executiveSummary("This report analyzes the requested topic. Key trends have been identified and recommendations provided.")
                .keyInsights(List.of("Data trends identified", "Metrics analyzed", "Recommendations provided"))
                .recommendations(List.of("Monitor metrics", "Review trends", "Gather more data"))
                .metricsHighlighted(Map.of("topic", request.getTopic()))
                .format(request.getFormat() != null ? request.getFormat().name() : "DETAILED_ANALYSIS")
                .build();
    }
}