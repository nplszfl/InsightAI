package com.insightai.forecasting.service;

import com.insightai.forecasting.dto.SeasonalityRequest;
import com.insightai.forecasting.dto.SeasonalityResponse;
import com.insightai.forecasting.entity.SeasonalityDetection;
import com.insightai.forecasting.mapper.SeasonalityDetectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonalityDetectionService {
    
    private final SeasonalityDetectionMapper seasonalityDetectionMapper;
    
    public SeasonalityResponse detect(SeasonalityRequest request) {
        log.info("Detecting seasonality for metric: {}", request.getMetricName());
        
        double strength = calculateSeasonalityStrength(request.getDataPoints());
        int periodDays = detectPeriod(request.getDataPoints());
        String pattern = classifyPattern(request.getDataPoints(), periodDays);
        String peakTime = findPeakTime(request.getDataPoints());
        String troughTime = findTroughTime(request.getDataPoints());
        
        SeasonalityDetection record = SeasonalityDetection.builder()
                .metricName(request.getMetricName())
                .seasonalityPattern(pattern)
                .strength(strength)
                .periodDays(periodDays)
                .peakTime(peakTime)
                .troughTime(troughTime)
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        seasonalityDetectionMapper.insert(record);
        
        return SeasonalityResponse.builder()
                .id(record.getId())
                .metricName(request.getMetricName())
                .seasonalityPattern(pattern)
                .strength(strength)
                .periodDays(periodDays)
                .peakTime(peakTime)
                .troughTime(troughTime)
                .build();
    }
    
    private double calculateSeasonalityStrength(List<SeasonalityRequest.DataPoint> dataPoints) {
        if (dataPoints.size() < 14) return 0.0;
        
        double mean = dataPoints.stream().mapToDouble(SeasonalityRequest.DataPoint::getValue).average().orElse(0);
        
        double sumPositive = 0, sumNegative = 0;
        int midPoint = dataPoints.size() / 2;
        
        for (int i = 0; i < midPoint; i++) {
            double diff = dataPoints.get(i + midPoint).getValue() - dataPoints.get(i).getValue();
            if (diff > 0) sumPositive += diff;
            else sumNegative += Math.abs(diff);
        }
        
        double totalVariation = dataPoints.stream()
                .mapToDouble(dp -> Math.abs(dp.getValue() - mean))
                .sum();
        
        return totalVariation == 0 ? 0 : (sumPositive + sumNegative) / (2 * totalVariation);
    }
    
    private int detectPeriod(List<SeasonalityRequest.DataPoint> dataPoints) {
        if (dataPoints.size() >= 365) return 365;
        if (dataPoints.size() >= 90) return 90;
        if (dataPoints.size() >= 30) return 30;
        if (dataPoints.size() >= 7) return 7;
        return 7;
    }
    
    private String classifyPattern(List<SeasonalityRequest.DataPoint> dataPoints, int periodDays) {
        double strength = calculateSeasonalityStrength(dataPoints);
        
        if (strength < 0.2) return "NONE";
        if (periodDays == 7) return "WEEKLY";
        if (periodDays == 30) return "MONTHLY";
        if (periodDays == 90) return "QUARTERLY";
        if (periodDays == 365) return "YEARLY";
        return "CUSTOM";
    }
    
    private String findPeakTime(List<SeasonalityRequest.DataPoint> dataPoints) {
        SeasonalityRequest.DataPoint peak = dataPoints.get(0);
        for (SeasonalityRequest.DataPoint dp : dataPoints) {
            if (dp.getValue() > peak.getValue()) {
                peak = dp;
            }
        }
        return peak.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    private String findTroughTime(List<SeasonalityRequest.DataPoint> dataPoints) {
        SeasonalityRequest.DataPoint trough = dataPoints.get(0);
        for (SeasonalityRequest.DataPoint dp : dataPoints) {
            if (dp.getValue() < trough.getValue()) {
                trough = dp;
            }
        }
        return trough.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}