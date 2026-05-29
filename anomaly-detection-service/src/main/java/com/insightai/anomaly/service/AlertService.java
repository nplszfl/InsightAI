package com.insightai.anomaly.service;

import com.insightai.anomaly.dto.AlertRequest;
import com.insightai.anomaly.dto.AlertResponse;
import com.insightai.anomaly.entity.AnomalyAlert;
import com.insightai.anomaly.mapper.AnomalyAlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {
    
    private final AnomalyAlertMapper anomalyAlertMapper;
    
    public AlertResponse sendAlert(AlertRequest request) {
        log.info("Sending alert for anomaly record: {}", request.getAnomalyRecordId());
        
        AnomalyAlert alert = AnomalyAlert.builder()
                .anomalyRecordId(request.getAnomalyRecordId())
                .alertType(request.getAlertType())
                .alertChannel(request.getAlertChannel())
                .recipient(request.getRecipient())
                .alertContent(request.getAlertContent() != null ? request.getAlertContent() : 
                        generateAlertContent(request))
                .alertStatus("PENDING")
                .retryCount(0)
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        anomalyAlertMapper.insert(alert);
        
        try {
            deliverAlert(alert);
            alert.setAlertStatus("SENT");
            alert.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to deliver alert: {}", e.getMessage());
            alert.setAlertStatus("FAILED");
            alert.setRetryCount(alert.getRetryCount() + 1);
        }
        
        anomalyAlertMapper.updateById(alert);
        
        return AlertResponse.builder()
                .id(alert.getId())
                .anomalyRecordId(alert.getAnomalyRecordId())
                .alertType(alert.getAlertType())
                .alertChannel(alert.getAlertChannel())
                .recipient(alert.getRecipient())
                .alertContent(alert.getAlertContent())
                .alertStatus(alert.getAlertStatus())
                .sentAt(alert.getSentAt())
                .build();
    }
    
    private String generateAlertContent(AlertRequest request) {
        return String.format("Anomaly Alert - Type: %s, Channel: %s, Recipient: %s",
                request.getAlertType(), request.getAlertChannel(), request.getRecipient());
    }
    
    private void deliverAlert(AnomalyAlert alert) {
        switch (alert.getAlertChannel()) {
            case "EMAIL":
                log.info("Sending email alert to: {}", alert.getRecipient());
                break;
            case "SMS":
                log.info("Sending SMS alert to: {}", alert.getRecipient());
                break;
            case "WEBHOOK":
                log.info("Sending webhook alert to: {}", alert.getRecipient());
                break;
            default:
                log.info("Sending default alert to: {}", alert.getRecipient());
        }
    }
}