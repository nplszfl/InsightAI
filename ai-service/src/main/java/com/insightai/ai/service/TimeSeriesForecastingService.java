package com.insightai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.ai.client.DeepSeekClient;
import com.insightai.ai.dto.TimeSeriesForecastRequest;
import com.insightai.ai.dto.TimeSeriesForecastResponse;
import com.insightai.ai.dto.TimeSeriesForecastResponse.ForecastPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TimeSeriesForecastingService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert in time series forecasting and data analysis. Your task is to analyze time series data and predict future trends.
            
            Output ONLY a valid JSON object with this structure:
            {
              "forecast": [
                {"timestamp": "2024-01-01T00:00:00", "predictedValue": 100.5, "lowerBound": 95.0, "upperBound": 106.0}
              ],
              "trendPercentage": 5.2,
              "trendDirection": "increasing",
              "seasonality": "quarterly",
              "confidence": "high",
              "modelUsed": "exponential smoothing"
            }
            
            Guidelines:
            - Analyze the historical data to identify trends, seasonality, and patterns
            - Generate forecasts for the requested number of periods
            - Provide confidence intervals for each prediction
            - Identify any seasonality patterns (daily, weekly, monthly, quarterly, yearly)
            - Use appropriate forecasting methods based on data characteristics
            """;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TimeSeriesForecastingService(DeepSeekClient deepSeekClient, ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    public TimeSeriesForecastResponse forecast(TimeSeriesForecastRequest request) {
        log.info("Generating forecast for {} data points over {} periods",
                request.getDataPoints().size(), request.getForecastPeriods());

        String prompt = buildPrompt(request);

        try {
            String jsonResponse = deepSeekClient.generateJsonCompletion(prompt, SYSTEM_PROMPT);
            return parseResponse(jsonResponse, request);
        } catch (Exception e) {
            log.error("Failed to generate forecast: {}", e.getMessage());
            return buildFallbackForecast(request);
        }
    }

    private String buildPrompt(TimeSeriesForecastRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following time series data and provide forecasts:\n\n");

        prompt.append("Historical Data:\n");
        request.getDataPoints().forEach(dp ->
                prompt.append(String.format("  %s: %.2f\n",
                        dp.getTimestamp().format(FORMATTER), dp.getValue()))
        );

        prompt.append("\nForecast Periods: ").append(request.getForecastPeriods()).append("\n");

        if (request.getConfidenceLevel() != null && !request.getConfidenceLevel().isEmpty()) {
            prompt.append("Requested Confidence Level: ").append(request.getConfidenceLevel()).append("\n");
        }

        prompt.append("\nProvide your forecast in JSON format with all required fields.");

        return prompt.toString();
    }

    private TimeSeriesForecastResponse parseResponse(String jsonResponse, TimeSeriesForecastRequest request) {
        try {
            return objectMapper.readValue(jsonResponse, TimeSeriesForecastResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, using fallback");
            return extractForecastManually(jsonResponse, request);
        }
    }

    private TimeSeriesForecastResponse extractForecastManually(String json, TimeSeriesForecastRequest request) {
        try {
            double lastValue = request.getDataPoints().get(request.getDataPoints().size() - 1).getValue();
            double trend = calculateSimpleTrend(request.getDataPoints());
            int periods = request.getForecastPeriods();

            List<ForecastPoint> forecastPoints = new ArrayList<>();
            java.time.LocalDateTime lastTimestamp = request.getDataPoints().get(request.getDataPoints().size() - 1).getTimestamp();

            for (int i = 1; i <= periods; i++) {
                double predictedValue = lastValue + (trend * i);
                double uncertainty = Math.abs(trend * i * 0.1);

                forecastPoints.add(ForecastPoint.builder()
                        .timestamp(lastTimestamp.plusDays(i))
                        .predictedValue(predictedValue)
                        .lowerBound(predictedValue - uncertainty)
                        .upperBound(predictedValue + uncertainty)
                        .build());
            }

            return TimeSeriesForecastResponse.builder()
                    .forecast(forecastPoints)
                    .trendPercentage(trend / lastValue * 100)
                    .trendDirection(trend >= 0 ? "increasing" : "decreasing")
                    .seasonality("not detected")
                    .confidence("medium")
                    .modelUsed("linear extrapolation")
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract forecast manually", e);
            return buildSimpleFallback(request);
        }
    }

    private double calculateSimpleTrend(List<TimeSeriesForecastRequest.DataPoint> dataPoints) {
        if (dataPoints.size() < 2) {
            return 0.0;
        }

        int n = dataPoints.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += dataPoints.get(i).getValue();
            sumXY += i * dataPoints.get(i).getValue();
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }

    private TimeSeriesForecastResponse buildFallbackForecast(TimeSeriesForecastRequest request) {
        return buildSimpleFallback(request);
    }

    private TimeSeriesForecastResponse buildSimpleFallback(TimeSeriesForecastRequest request) {
        try {
            double trend = calculateSimpleTrend(request.getDataPoints());
            double lastValue = request.getDataPoints().get(request.getDataPoints().size() - 1).getValue();
            int periods = request.getForecastPeriods();
            java.time.LocalDateTime lastTimestamp = request.getDataPoints().get(request.getDataPoints().size() - 1).getTimestamp();

            List<ForecastPoint> forecastPoints = new ArrayList<>();
            for (int i = 1; i <= periods; i++) {
                forecastPoints.add(ForecastPoint.builder()
                        .timestamp(lastTimestamp.plusDays(i))
                        .predictedValue(lastValue + (trend * i))
                        .lowerBound(lastValue + (trend * i) * 0.9)
                        .upperBound(lastValue + (trend * i) * 1.1)
                        .build());
            }

            return TimeSeriesForecastResponse.builder()
                    .forecast(forecastPoints)
                    .trendPercentage(Math.abs(trend / lastValue * 100))
                    .trendDirection(trend >= 0 ? "increasing" : "decreasing")
                    .seasonality("not detected")
                    .confidence("medium")
                    .modelUsed("linear regression")
                    .build();
        } catch (Exception e) {
            log.error("Failed to build fallback forecast", e);
            return TimeSeriesForecastResponse.builder()
                    .forecast(List.of())
                    .trendPercentage(0.0)
                    .trendDirection("unknown")
                    .seasonality("unknown")
                    .confidence("low")
                    .modelUsed("none")
                    .build();
        }
    }
}