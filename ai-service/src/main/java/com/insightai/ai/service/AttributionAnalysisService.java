package com.insightai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.ai.client.DeepSeekClient;
import com.insightai.ai.dto.AttributionAnalysisRequest;
import com.insightai.ai.dto.AttributionAnalysisResponse;
import com.insightai.ai.dto.AttributionAnalysisResponse.AttributionFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AttributionAnalysisService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert in business analytics and attribution analysis. Your task is to explain why metrics have changed by identifying and quantifying contributing factors.
            
            Output ONLY a valid JSON object with this structure:
            {
              "metricName": "revenue",
              "changeAmount": 15000.0,
              "changePercentage": 12.5,
              "direction": "increase",
              "attributedFactors": [
                {
                  "factor": "Marketing Campaign",
                  "contribution": 8000.0,
                  "percentageContribution": 53.3,
                  "description": "New campaign drove additional sales"
                }
              ],
              "explanation": "Detailed explanation of why the metric changed...",
              "recommendations": ["recommendation 1", "recommendation 2"]
            }
            
            Guidelines:
            - Accurately calculate the change amount and percentage
            - Distribute attribution across contributing factors (total should be 100% or close)
            - Provide clear, actionable explanations for each factor
            - Suggest recommendations based on the analysis
            - Consider both positive and negative contributors
            """;

    public AttributionAnalysisService(DeepSeekClient deepSeekClient, ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    public AttributionAnalysisResponse analyze(AttributionAnalysisRequest request) {
        log.info("Analyzing attribution for metric: {} ({} -> {})",
                request.getMetricName(), request.getPreviousValue(), request.getCurrentValue());

        String prompt = buildPrompt(request);

        try {
            String jsonResponse = deepSeekClient.generateJsonCompletion(prompt, SYSTEM_PROMPT);
            return parseResponse(jsonResponse, request);
        } catch (Exception e) {
            log.error("Failed to analyze attribution: {}", e.getMessage());
            return buildFallbackAnalysis(request);
        }
    }

    private String buildPrompt(AttributionAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the change in metric: ").append(request.getMetricName()).append("\n\n");

        prompt.append("Current Value: ").append(request.getCurrentValue()).append("\n");
        prompt.append("Previous Value: ").append(request.getPreviousValue()).append("\n");
        double changeAmount = request.getCurrentValue() - request.getPreviousValue();
        double changePercentage = request.getPreviousValue() != 0
                ? (changeAmount / request.getPreviousValue()) * 100
                : 0;
        prompt.append(String.format("Change: %.2f (%.2f%%)\n\n", changeAmount, changePercentage));

        if (request.getTimeRange() != null && !request.getTimeRange().isEmpty()) {
            prompt.append("Time Range: ").append(request.getTimeRange()).append("\n\n");
        }

        if (request.getContributingFactors() != null && !request.getContributingFactors().isEmpty()) {
            prompt.append("Contributing Factors:\n");
            request.getContributingFactors().forEach(factor -> {
                prompt.append("  - ").append(factor).append("\n");
            });
            prompt.append("\n");
        }

        if (request.getAnalysisType() != null && !request.getAnalysisType().isEmpty()) {
            prompt.append("Analysis Type: ").append(request.getAnalysisType()).append("\n");
        }

        prompt.append("\nProvide your attribution analysis in JSON format.");

        return prompt.toString();
    }

    private AttributionAnalysisResponse parseResponse(String jsonResponse, AttributionAnalysisRequest request) {
        try {
            AttributionAnalysisResponse response = objectMapper.readValue(jsonResponse, AttributionAnalysisResponse.class);
            validateResponse(response);
            return response;
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, using fallback");
            return extractAnalysisManually(jsonResponse, request);
        }
    }

    private void validateResponse(AttributionAnalysisResponse response) {
        if (response.getMetricName() == null) {
            response.setMetricName("unknown");
        }
        if (response.getDirection() == null) {
            double change = response.getChangeAmount();
            response.setDirection(change >= 0 ? "increase" : "decrease");
        }
    }

    private AttributionAnalysisResponse extractAnalysisManually(String json, AttributionAnalysisRequest request) {
        String metricName = extractJsonValue(json, "metricName");
        double changeAmount = request.getCurrentValue() - request.getPreviousValue();
        double changePercentage = request.getPreviousValue() != 0
                ? (changeAmount / request.getPreviousValue()) * 100
                : 0;

        List<AttributionFactor> factors = extractFactors(json, Math.abs(changeAmount));

        return AttributionAnalysisResponse.builder()
                .metricName(metricName != null ? metricName : request.getMetricName())
                .changeAmount(changeAmount)
                .changePercentage(changePercentage)
                .direction(changeAmount >= 0 ? "increase" : "decrease")
                .attributedFactors(factors)
                .explanation("The metric changed due to multiple contributing factors. See detailed breakdown for more information.")
                .recommendations(List.of("Continue monitoring", "Analyze contributing factors further", "Review strategy"))
                .build();
    }

    private List<AttributionFactor> extractFactors(String json, double totalChange) {
        List<AttributionFactor> factors = new ArrayList<>();

        String pattern = "\"factor\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        int factorIndex = 0;
        String[] defaultFactors = {"Market conditions", "Campaign performance", "Seasonal effects", "Operational changes"};

        while (m.find() && factorIndex < defaultFactors.length) {
            String factorName = m.group(1);
            double contribution = totalChange * 0.25;
            double percentage = 25.0;

            factors.add(AttributionFactor.builder()
                    .factor(factorName)
                    .contribution(contribution)
                    .percentageContribution(percentage)
                    .description("Factor contributing to metric change")
                    .build());

            factorIndex++;
        }

        if (factors.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                factors.add(AttributionFactor.builder()
                        .factor(defaultFactors[i])
                        .contribution(totalChange * 0.25)
                        .percentageContribution(25.0)
                        .description("Contributing factor " + (i + 1))
                        .build());
            }
        }

        return factors;
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

    private AttributionAnalysisResponse buildFallbackAnalysis(AttributionAnalysisRequest request) {
        double changeAmount = request.getCurrentValue() - request.getPreviousValue();
        double changePercentage = request.getPreviousValue() != 0
                ? (changeAmount / request.getPreviousValue()) * 100
                : 0;

        List<AttributionFactor> factors = new ArrayList<>();

        if (request.getContributingFactors() != null && !request.getContributingFactors().isEmpty()) {
            double totalContributions = request.getContributingFactors().size();
            for (Map<String, Object> factor : request.getContributingFactors()) {
                String factorName = factor.toString();
                double contribution = Math.abs(changeAmount / totalContributions);
                double percentage = 100.0 / totalContributions;

                factors.add(AttributionFactor.builder()
                        .factor(factorName)
                        .contribution(contribution)
                        .percentageContribution(percentage)
                        .description("Contributing to metric change")
                        .build());
            }
        } else {
            factors.add(AttributionFactor.builder()
                    .factor("Other factors")
                    .contribution(Math.abs(changeAmount))
                    .percentageContribution(100.0)
                    .description("Unspecified contributing factors")
                    .build());
        }

        String direction = changeAmount >= 0 ? "increase" : "decrease";
        String explanation = String.format(
                "The metric '%s' showed a %s of %.2f (%.2f%%) compared to the previous period. %s",
                request.getMetricName(),
                direction,
                Math.abs(changeAmount),
                Math.abs(changePercentage),
                changeAmount >= 0
                        ? "This positive change requires further analysis to understand driving factors."
                        : "This negative change warrants investigation into potential causes."
        );

        return AttributionAnalysisResponse.builder()
                .metricName(request.getMetricName())
                .changeAmount(changeAmount)
                .changePercentage(changePercentage)
                .direction(direction)
                .attributedFactors(factors)
                .explanation(explanation)
                .recommendations(List.of(
                        "Monitor the trend closely",
                        "Investigate primary contributing factors",
                        "Consider implementing corrective actions if negative",
                        "Document findings for future reference"
                ))
                .build();
    }
}