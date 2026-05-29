package com.insightai.anomaly.service;

import com.insightai.anomaly.dto.AnomalyDetectionRequest;
import com.insightai.anomaly.dto.AnomalyResponse;
import com.insightai.anomaly.entity.AnomalyRecord;
import com.insightai.anomaly.mapper.AnomalyRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {
    
    private final AnomalyRecordMapper anomalyRecordMapper;
    
    public List<AnomalyResponse> detect(AnomalyDetectionRequest request) {
        log.info("Detecting anomalies for metric: {}", request.getMetricName());
        
        List<AnomalyDetectionRequest.DataPoint> dataPoints = request.getDataPoints();
        double sensitivity = request.getSensitivity() != null ? request.getSensitivity() : 2.0;
        String method = request.getDetectionMethod() != null ? request.getDetectionMethod() : "STATISTICAL";
        
        double mean = calculateMean(dataPoints);
        double stdDev = calculateStdDev(dataPoints, mean);
        double threshold = stdDev * sensitivity;
        
        List<AnomalyResponse> anomalies = new ArrayList<>();
        
        for (AnomalyDetectionRequest.DataPoint dp : dataPoints) {
            double deviation = Math.abs(dp.getValue() - mean);
            
            if (deviation > threshold) {
                String anomalyType = classifyAnomaly(dp.getValue(), mean, deviation);
                double severity = calculateSeverity(deviation, stdDev);
                
                AnomalyRecord record = AnomalyRecord.builder()
                        .metricName(request.getMetricName())
                        .detectionTime(dp.getTimestamp())
                        .metricValue(BigDecimal.valueOf(dp.getValue()).setScale(2, RoundingMode.HALF_UP))
                        .threshold(BigDecimal.valueOf(threshold).setScale(2, RoundingMode.HALF_UP))
                        .anomalyType(anomalyType)
                        .severity(severity)
                        .description(String.format("Value %.2f deviates from mean %.2f by %.2f (threshold: %.2f)",
                                dp.getValue(), mean, deviation, threshold))
                        .acknowledged(false)
                        .createdBy(request.getCreatedBy())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                anomalyRecordMapper.insert(record);
                
                anomalies.add(AnomalyResponse.builder()
                        .id(record.getId())
                        .metricName(request.getMetricName())
                        .detectionTime(dp.getTimestamp())
                        .metricValue(BigDecimal.valueOf(dp.getValue()).setScale(2, RoundingMode.HALF_UP))
                        .threshold(BigDecimal.valueOf(threshold).setScale(2, RoundingMode.HALF_UP))
                        .anomalyType(anomalyType)
                        .severity(severity)
                        .description(record.getDescription())
                        .build());
            }
        }
        
        return anomalies;
    }
    
    private double calculateMean(List<AnomalyDetectionRequest.DataPoint> dataPoints) {
        return dataPoints.stream()
                .mapToDouble(AnomalyDetectionRequest.DataPoint::getValue)
                .average()
                .orElse(0.0);
    }
    
    private double calculateStdDev(List<AnomalyDetectionRequest.DataPoint> dataPoints, double mean) {
        double variance = dataPoints.stream()
                .mapToDouble(dp -> Math.pow(dp.getValue() - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
    
    private String classifyAnomaly(double value, double mean, double deviation) {
        if (value > mean * 1.5) return "SPIKE";
        if (value < mean * 0.5) return "DROP";
        if (deviation > mean * 0.3) return "OUTLIER";
        return "ANOMALY";
    }
    
    private double calculateSeverity(double deviation, double stdDev) {
        if (stdDev == 0) return 1.0;
        double severity = Math.min(1.0, (deviation / stdDev) / 10.0);
        return severity;
    }
}