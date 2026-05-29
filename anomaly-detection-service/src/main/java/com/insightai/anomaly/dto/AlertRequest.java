package com.insightai.anomaly.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRequest {
    @NotNull(message = "Anomaly record ID is required")
    private Long anomalyRecordId;
    
    @NotBlank(message = "Alert type is required")
    private String alertType;
    
    @NotBlank(message = "Alert channel is required")
    private String alertChannel;
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
    
    private String alertContent;
    private String createdBy;
}