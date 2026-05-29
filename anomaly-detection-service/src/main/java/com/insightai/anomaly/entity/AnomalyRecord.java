package com.insightai.anomaly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("anomaly_record")
public class AnomalyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String metricName;
    private LocalDateTime detectionTime;
    private BigDecimal metricValue;
    private BigDecimal threshold;
    private String anomalyType;
    private Double severity;
    private String description;
    private Boolean acknowledged;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}