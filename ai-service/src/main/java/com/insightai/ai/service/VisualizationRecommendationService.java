package com.insightai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.ai.client.DeepSeekClient;
import com.insightai.ai.dto.VisualizationRecommendationRequest;
import com.insightai.ai.dto.VisualizationRecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VisualizationRecommendationService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert data visualization consultant. Your task is to recommend the best chart types based on the data structure and query context.
            
            Output ONLY a valid JSON object with this structure:
            {
              "recommendedChartType": "bar" | "line" | "pie" | "scatter" | "area" | "heatmap" | "box" | "histogram",
              "alternativeChartTypes": ["chart_type_1", "chart_type_2"],
              "reasoning": "Detailed explanation of why this chart type is recommended",
              "axisMapping": {
                "xAxis": "column_name",
                "yAxis": "column_name",
                "colorBy": "column_name (optional)",
                "sizeBy": "column_name (optional)"
              }
            }
            
            Chart type selection guidelines:
            - bar: Categorical data comparison, rankings
            - line: Time series data, trends over time
            - pie: Part-to-whole relationships, limited categories
            - scatter: Correlation analysis, two continuous variables
            - area: Cumulative data, stacked comparisons
            - heatmap: Data density, correlation matrices
            - box: Statistical distribution, outliers
            - histogram: Frequency distribution
            """;

    public VisualizationRecommendationService(DeepSeekClient deepSeekClient, ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    public VisualizationRecommendationResponse recommend(VisualizationRecommendationRequest request) {
        log.info("Generating visualization recommendation for {} columns", request.getColumns().size());

        String prompt = buildPrompt(request);

        try {
            String jsonResponse = deepSeekClient.generateJsonCompletion(prompt, SYSTEM_PROMPT);
            return parseResponse(jsonResponse);
        } catch (Exception e) {
            log.error("Failed to generate visualization recommendation: {}", e.getMessage());
            return buildFallbackResponse(request);
        }
    }

    private String buildPrompt(VisualizationRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following data columns and recommend the best visualization:\n\n");

        String columnsJson = request.getColumns().stream()
                .map(c -> String.format("  - Name: %s, Type: %s, Role: %s",
                        c.getName(), c.getDataType(), c.getRole() != null ? c.getRole() : "undefined"))
                .collect(Collectors.joining("\n"));

        prompt.append("Columns:\n").append(columnsJson).append("\n\n");

        if (request.getQueryContext() != null && !request.getQueryContext().isEmpty()) {
            prompt.append("Query Context: ").append(request.getQueryContext()).append("\n");
        }

        prompt.append("\nProvide your recommendation in JSON format.");

        return prompt.toString();
    }

    private VisualizationRecommendationResponse parseResponse(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, VisualizationRecommendationResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, using fallback");
            return extractRecommendationManually(jsonResponse);
        }
    }

    private VisualizationRecommendationResponse extractRecommendationManually(String json) {
        String chartType = extractJsonValue(json, "recommendedChartType");
        String reasoning = extractJsonValue(json, "reasoning");

        return VisualizationRecommendationResponse.builder()
                .recommendedChartType(chartType != null ? chartType : "bar")
                .alternativeChartTypes(List.of("line", "pie"))
                .reasoning(reasoning != null ? reasoning : "Default fallback recommendation")
                .axisMapping(Map.of("xAxis", "category", "yAxis", "value"))
                .build();
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^\",}\\]]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    private VisualizationRecommendationResponse buildFallbackResponse(VisualizationRecommendationRequest request) {
        String recommendedType = inferBasicChartType(request.getColumns());

        return VisualizationRecommendationResponse.builder()
                .recommendedChartType(recommendedType)
                .alternativeChartTypes(List.of("bar", "line"))
                .reasoning("Based on column data types, " + recommendedType + " chart is recommended.")
                .axisMapping(Map.of("xAxis", request.getColumns().get(0).getName(), "yAxis", "value"))
                .build();
    }

    private String inferBasicChartType(List<VisualizationRecommendationRequest.ColumnDefinition> columns) {
        long dateColumns = columns.stream()
                .filter(c -> c.getDataType() != null &&
                        (c.getDataType().toLowerCase().contains("date") ||
                         c.getDataType().toLowerCase().contains("time")))
                .count();

        if (dateColumns > 0) {
            return "line";
        }

        long numericColumns = columns.stream()
                .filter(c -> c.getDataType() != null &&
                        (c.getDataType().toLowerCase().contains("int") ||
                         c.getDataType().toLowerCase().contains("float") ||
                         c.getDataType().toLowerCase().contains("double")))
                .count();

        if (numericColumns > 1) {
            return "scatter";
        }

        return "bar";
    }
}