package com.insightai.forecasting.service;

import com.insightai.forecasting.dto.TrendAnalysisRequest;
import com.insightai.forecasting.dto.TrendAnalysisResponse;
import com.insightai.forecasting.mapper.TrendAnalysisMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendAnalysisServiceTest {
    
    @Mock
    private TrendAnalysisMapper trendAnalysisMapper;
    
    @InjectMocks
    private TrendAnalysisService trendAnalysisService;
    
    private TrendAnalysisRequest request;
    private List<TrendAnalysisRequest.DataPoint> dataPoints;
    
    @BeforeEach
    void setUp() {
        dataPoints = new ArrayList<>();
        dataPoints.add(TrendAnalysisRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(3))
                .value(100.0)
                .build());
        dataPoints.add(TrendAnalysisRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(2))
                .value(110.0)
                .build());
        dataPoints.add(TrendAnalysisRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(1))
                .value(120.0)
                .build());
        
        request = TrendAnalysisRequest.builder()
                .metricName("test_metric")
                .dataPoints(dataPoints)
                .createdBy("test_user")
                .build();
    }
    
    @Test
    void testAnalyze_ShouldReturnTrendAnalysis() {
        when(trendAnalysisMapper.insert(any())).thenAnswer(invocation -> {
            var record = invocation.getArgument(0, com.insightai.forecasting.entity.TrendAnalysis.class);
            record.setId(1L);
            return 1;
        });
        
        TrendAnalysisResponse result = trendAnalysisService.analyze(request);
        
        assertNotNull(result);
        assertEquals("test_metric", result.getMetricName());
        assertNotNull(result.getTrendType());
        assertNotNull(result.getSlope());
        verify(trendAnalysisMapper, times(1)).insert(any());
    }
}