package com.insightai.forecasting.service;

import com.insightai.forecasting.dto.ForecastRequest;
import com.insightai.forecasting.dto.ForecastResponse;
import com.insightai.forecasting.mapper.ForecastRecordMapper;
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
class ForecastingServiceTest {
    
    @Mock
    private ForecastRecordMapper forecastRecordMapper;
    
    @InjectMocks
    private ForecastingService forecastingService;
    
    private ForecastRequest request;
    private List<ForecastRequest.DataPoint> dataPoints;
    
    @BeforeEach
    void setUp() {
        dataPoints = new ArrayList<>();
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(6))
                .value(100.0)
                .build());
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(5))
                .value(110.0)
                .build());
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(4))
                .value(120.0)
                .build());
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(3))
                .value(130.0)
                .build());
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(2))
                .value(125.0)
                .build());
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now().minusDays(1))
                .value(140.0)
                .build());
        dataPoints.add(ForecastRequest.DataPoint.builder()
                .timestamp(LocalDateTime.now())
                .value(150.0)
                .build());
        
        request = ForecastRequest.builder()
                .metricName("test_metric")
                .dataPoints(dataPoints)
                .forecastDays(3)
                .confidenceLevel(0.95)
                .createdBy("test_user")
                .build();
    }
    
    @Test
    void testForecast_ShouldReturnForecasts() {
        when(forecastRecordMapper.insert(any())).thenAnswer(invocation -> {
            var record = invocation.getArgument(0, com.insightai.forecasting.entity.ForecastRecord.class);
            record.setId(1L);
            return 1;
        });
        
        List<ForecastResponse> result = forecastingService.forecast(request);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("test_metric", result.get(0).getMetricName());
        verify(forecastRecordMapper, times(3)).insert(any());
    }
    
    @Test
    void testForecast_WithEmptyDataPoints_ShouldThrowException() {
        request.setDataPoints(new ArrayList<>());
        
        assertThrows(Exception.class, () -> forecastingService.forecast(request));
    }
}