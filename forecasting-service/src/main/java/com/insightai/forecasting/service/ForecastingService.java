package com.insightai.forecasting.service;

import com.insightai.forecasting.dto.ForecastRequest;
import com.insightai.forecasting.dto.ForecastResponse;
import com.insightai.forecasting.entity.ForecastRecord;
import com.insightai.forecasting.mapper.ForecastRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastingService {
    
    private final ForecastRecordMapper forecastRecordMapper;
    
    public List<ForecastResponse> forecast(ForecastRequest request) {
        log.info("Forecasting for metric: {}", request.getMetricName());
        
        List<ForecastRequest.DataPoint> dataPoints = request.getDataPoints();
        int forecastDays = request.getForecastDays() != null ? request.getForecastDays() : 7;
        double confidenceLevel = request.getConfidenceLevel() != null ? request.getConfidenceLevel() : 0.95;
        
        double mean = calculateMean(dataPoints);
        double stdDev = calculateStdDev(dataPoints, mean);
        
        String trendDirection = detectTrend(dataPoints);
        double seasonalityScore = calculateSeasonalityScore(dataPoints);
        
        LocalDateTime lastTime = dataPoints.get(dataPoints.size() - 1).getTimestamp();
        List<ForecastResponse> forecasts = new ArrayList<>();
        
        for (int i = 1; i <= forecastDays; i++) {
            LocalDateTime forecastTime = lastTime.plusDays(i);
            double predictedValue = mean + (i * 0.1 * (trendDirection.equals("UP") ? 1 : -1));
            double confidenceInterval = stdDev * 1.96;
            
            ForecastRecord record = ForecastRecord.builder()
                    .metricName(request.getMetricName())
                    .timePoint(forecastTime)
                    .predictedValue(BigDecimal.valueOf(predictedValue).setScale(2, RoundingMode.HALF_UP))
                    .confidenceInterval(BigDecimal.valueOf(confidenceInterval).setScale(2, RoundingMode.HALF_UP))
                    .forecastPeriod(forecastDays + " days")
                    .trendDirection(trendDirection)
                    .seasonalityScore(seasonalityScore)
                    .createdBy(request.getCreatedBy())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            forecastRecordMapper.insert(record);
            
            forecasts.add(ForecastResponse.builder()
                    .id(record.getId())
                    .metricName(request.getMetricName())
                    .forecastTime(forecastTime)
                    .predictedValue(BigDecimal.valueOf(predictedValue).setScale(2, RoundingMode.HALF_UP))
                    .confidenceInterval(BigDecimal.valueOf(confidenceInterval).setScale(2, RoundingMode.HALF_UP))
                    .trendDirection(trendDirection)
                    .seasonalityScore(seasonalityScore)
                    .build());
        }
        
        return forecasts;
    }
    
    private double calculateMean(List<ForecastRequest.DataPoint> dataPoints) {
        return dataPoints.stream()
                .mapToDouble(ForecastRequest.DataPoint::getValue)
                .average()
                .orElse(0.0);
    }
    
    private double calculateStdDev(List<ForecastRequest.DataPoint> dataPoints, double mean) {
        double variance = dataPoints.stream()
                .mapToDouble(dp -> Math.pow(dp.getValue() - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
    
    private String detectTrend(List<ForecastRequest.DataPoint> dataPoints) {
        if (dataPoints.size() < 2) return "STABLE";
        
        double first = dataPoints.get(0).getValue();
        double last = dataPoints.get(dataPoints.size() - 1).getValue();
        double diff = last - first;
        
        if (diff > first * 0.05) return "UP";
        if (diff < -first * 0.05) return "DOWN";
        return "STABLE";
    }
    
    private double calculateSeasonalityScore(List<ForecastRequest.DataPoint> dataPoints) {
        if (dataPoints.size() < 7) return 0.0;
        
        double sum = 0;
        for (int i = 0; i < dataPoints.size(); i++) {
            sum += dataPoints.get(i).getValue();
        }
        double mean = sum / dataPoints.size();
        
        double autocorrelation = 0;
        int lag = 7;
        if (dataPoints.size() > lag) {
            for (int i = 0; i < dataPoints.size() - lag; i++) {
                autocorrelation += (dataPoints.get(i).getValue() - mean) * 
                                   (dataPoints.get(i + lag).getValue() - mean);
            }
            autocorrelation /= (dataPoints.size() - lag);
        }
        
        double variance = dataPoints.stream()
                .mapToDouble(dp -> Math.pow(dp.getValue() - mean, 2))
                .average()
                .orElse(1.0);
        
        return Math.min(1.0, Math.abs(autocorrelation) / variance);
    }
}