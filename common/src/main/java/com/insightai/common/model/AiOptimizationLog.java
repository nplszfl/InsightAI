package com.insightai.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_optimization_log")
public class AiOptimizationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String originalQuery;
    private String optimizedQuery;
    private String optimizationType; // SYNTAX, PERFORMANCE, CACHE_HINT
    private String aiModel;
    private Double confidence;
    private String feedback; // User feedback on optimization
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
