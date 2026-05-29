package com.insightai.anomaly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("anomaly_alert")
public class AnomalyAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long anomalyRecordId;
    private String alertType;
    private String alertChannel;
    private String recipient;
    private String alertContent;
    private String alertStatus;
    private LocalDateTime sentAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private Integer retryCount;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}