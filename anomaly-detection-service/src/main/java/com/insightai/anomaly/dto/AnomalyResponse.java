package com.insightai.anomaly.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyResponse {
    private Long id;
    private String metricName;
    private LocalDateTime detectionTime;
    private BigDecimal metricValue;
    private BigDecimal threshold;
    private String anomalyType;
    private Double severity;
    private String description;
}