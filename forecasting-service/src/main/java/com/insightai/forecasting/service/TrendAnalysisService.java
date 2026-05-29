package com.insightai.forecasting.service;

import com.insightai.forecasting.dto.TrendAnalysisRequest;
import com.insightai.forecasting.dto.TrendAnalysisResponse;
import com.insightai.forecasting.entity.TrendAnalysis;
import com.insightai.forecasting.mapper.TrendAnalysisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendAnalysisService {
    
    private final TrendAnalysisMapper trendAnalysisMapper;
    
    public TrendAnalysisResponse analyze(TrendAnalysisRequest request) {
        log.info("Analyzing trend for metric: {}", request.getMetricName());
        
        double slope = calculateSlope(request.getDataPoints());
        double intercept = calculateIntercept(request.getDataPoints(), slope);
        double rSquared = calculateRSquared(request.getDataPoints(), slope, intercept);
        String trendType = classifyTrend(slope, rSquared);
        
        TrendAnalysis record = TrendAnalysis.builder()
                .metricName(request.getMetricName())
                .trendType(trendType)
                .slope(BigDecimal.valueOf(slope).setScale(4, RoundingMode.HALF_UP))
                .intercept(BigDecimal.valueOf(intercept).setScale(4, RoundingMode.HALF_UP))
                .rSquared(rSquared)
                .analysisPeriodStart(request.getPeriodStart() != null ? request.getPeriodStart() : 
                        request.getDataPoints().get(0).getTimestamp())
                .analysisPeriodEnd(request.getPeriodEnd() != null ? request.getPeriodEnd() : 
                        request.getDataPoints().get(request.getDataPoints().size() - 1).getTimestamp())
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        trendAnalysisMapper.insert(record);
        
        return TrendAnalysisResponse.builder()
                .id(record.getId())
                .metricName(request.getMetricName())
                .trendType(trendType)
                .slope(BigDecimal.valueOf(slope).setScale(4, RoundingMode.HALF_UP))
                .intercept(BigDecimal.valueOf(intercept).setScale(4, RoundingMode.HALF_UP))
                .rSquared(rSquared)
                .analysisPeriodStart(record.getAnalysisPeriodStart())
                .analysisPeriodEnd(record.getAnalysisPeriodEnd())
                .build();
    }
    
    private double calculateSlope(TrendAnalysisRequest.DataPoint[] dataPoints) {
        int n = dataPoints.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = dataPoints[i].getValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
    
    private double calculateIntercept(TrendAnalysisRequest.DataPoint[] dataPoints, double slope) {
        double meanY = 0;
        for (TrendAnalysisRequest.DataPoint dp : dataPoints) {
            meanY += dp.getValue();
        }
        meanY /= dataPoints.length;
        
        double meanX = (dataPoints.length - 1) / 2.0;
        return meanY - slope * meanX;
    }
    
    private double calculateRSquared(TrendAnalysisRequest.DataPoint[] dataPoints, double slope, double intercept) {
        double meanY = 0;
        for (TrendAnalysisRequest.DataPoint dp : dataPoints) {
            meanY += dp.getValue();
        }
        meanY /= dataPoints.length;
        
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < dataPoints.length; i++) {
            double yPred = slope * i + intercept;
            ssTot += Math.pow(dataPoints[i].getValue() - meanY, 2);
            ssRes += Math.pow(dataPoints[i].getValue() - yPred, 2);
        }
        
        return ssTot == 0 ? 0 : 1 - (ssRes / ssTot);
    }
    
    private String classifyTrend(double slope, double rSquared) {
        if (rSquared < 0.3) return "NO_TREND";
        
        if (slope > 0.1) return "UPWARD";
        if (slope < -0.1) return "DOWNWARD";
        return "STABLE";
    }
}