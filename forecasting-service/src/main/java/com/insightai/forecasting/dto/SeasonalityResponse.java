package com.insightai.forecasting.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalityResponse {
    private Long id;
    private String metricName;
    private String seasonalityPattern;
    private Double strength;
    private Integer periodDays;
    private String peakTime;
    private String troughTime;
}