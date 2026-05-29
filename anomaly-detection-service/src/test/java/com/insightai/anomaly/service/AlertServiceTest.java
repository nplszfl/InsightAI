package com.insightai.anomaly.service;

import com.insightai.anomaly.dto.AlertRequest;
import com.insightai.anomaly.dto.AlertResponse;
import com.insightai.anomaly.mapper.AnomalyAlertMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {
    
    @Mock
    private AnomalyAlertMapper anomalyAlertMapper;
    
    @InjectMocks
    private AlertService alertService;
    
    private AlertRequest request;
    
    @BeforeEach
    void setUp() {
        request = AlertRequest.builder()
                .anomalyRecordId(1L)
                .alertType("CRITICAL")
                .alertChannel("EMAIL")
                .recipient("admin@example.com")
                .createdBy("test_user")
                .build();
    }
    
    @Test
    void testSendAlert_ShouldReturnAlertResponse() {
        when(anomalyAlertMapper.insert(any())).thenAnswer(invocation -> {
            var alert = invocation.getArgument(0, com.insightai.anomaly.entity.AnomalyAlert.class);
            alert.setId(1L);
            return 1;
        });
        when(anomalyAlertMapper.updateById(any())).thenReturn(1);
        
        AlertResponse result = alertService.sendAlert(request);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMAIL", result.getAlertChannel());
        verify(anomalyAlertMapper, times(1)).insert(any());
    }
}