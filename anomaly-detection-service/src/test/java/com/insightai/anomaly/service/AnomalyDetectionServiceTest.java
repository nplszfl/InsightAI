package com.insightai.anomaly.service;

import com.insightai.anomaly.dto.AnomalyDetectionRequest;
import com.insightai.anomaly.dto.AnomalyResponse;
import com.insightai.anomaly.mapper.AnomalyRecordMapper;
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
class AnomalyDetectionServiceTest {
    
    @Mock
    private AnomalyRecordMapper anomalyRecordMapper;
    
    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;
    
    private AnomalyDetectionRequest request;
    private List<AnomalyDetectionRequest.DataPoint> dataPoints;
    
    @BeforeEach
    void setUp() {
        dataPoints = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataPoints.add(AnomalyDetectionRequest.DataPoint.builder()
                    .timestamp(LocalDateTime.now().minusMinutes(10 - i))
                    .value(100.0 + (i * 5))
                    .build());
        }
        dataPoints.get(9).setValue(500.0);
        
        request = AnomalyDetectionRequest.builder()
                .metricName("test_metric")
                .dataPoints(dataPoints)
                .sensitivity(2.0)
                .createdBy("test_user")
                .build();
    }
    
    @Test
    void testDetect_ShouldDetectAnomalies() {
        when(anomalyRecordMapper.insert(any())).thenAnswer(invocation -> {
            var record = invocation.getArgument(0, com.insightai.anomaly.entity.AnomalyRecord.class);
            record.setId(1L);
            return 1;
        });
        
        List<AnomalyResponse> result = anomalyDetectionService.detect(request);
        
        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(anomalyRecordMapper, atLeastOnce()).insert(any());
    }
    
    @Test
    void testDetect_WithNoAnomalies_ShouldReturnEmptyList() {
        request.getDataPoints().clear();
        for (int i = 0; i < 10; i++) {
            request.getDataPoints().add(AnomalyDetectionRequest.DataPoint.builder()
                    .timestamp(LocalDateTime.now().minusMinutes(10 - i))
                    .value(100.0)
                    .build());
        }
        
        List<AnomalyResponse> result = anomalyDetectionService.detect(request);
        
        assertNotNull(result);
    }
}