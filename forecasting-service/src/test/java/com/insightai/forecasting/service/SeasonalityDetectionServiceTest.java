package com.insightai.forecasting.service;

import com.insightai.forecasting.dto.SeasonalityRequest;
import com.insightai.forecasting.dto.SeasonalityResponse;
import com.insightai.forecasting.mapper.SeasonalityDetectionMapper;
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
class SeasonalityDetectionServiceTest {
    
    @Mock
    private SeasonalityDetectionMapper seasonalityDetectionMapper;
    
    @InjectMocks
    private SeasonalityDetectionService seasonalityDetectionService;
    
    private SeasonalityRequest request;
    private List<SeasonalityRequest.DataPoint> dataPoints;
    
    @BeforeEach
    void setUp() {
        dataPoints = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now();
        for (int i = 0; i < 14; i++) {
            dataPoints.add(SeasonalityRequest.DataPoint.builder()
                    .timestamp(base.minusDays(13 - i))
                    .value(100.0 + (i % 7) * 10.0)
                    .build());
        }
        
        request = SeasonalityRequest.builder()
                .metricName("test_metric")
                .dataPoints(dataPoints)
                .createdBy("test_user")
                .build();
    }
    
    @Test
    void testDetect_ShouldReturnSeasonality() {
        when(seasonalityDetectionMapper.insert(any())).thenAnswer(invocation -> {
            var record = invocation.getArgument(0, com.insightai.forecasting.entity.SeasonalityDetection.class);
            record.setId(1L);
            return 1;
        });
        
        SeasonalityResponse result = seasonalityDetectionService.detect(request);
        
        assertNotNull(result);
        assertEquals("test_metric", result.getMetricName());
        assertNotNull(result.getSeasonalityPattern());
        assertNotNull(result.getStrength());
        verify(seasonalityDetectionMapper, times(1)).insert(any());
    }
}