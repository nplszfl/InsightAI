package com.insightai.anomaly.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private Long id;
    private Long anomalyRecordId;
    private String alertType;
    private String alertChannel;
    private String recipient;
    private String alertContent;
    private String alertStatus;
    private LocalDateTime sentAt;
}