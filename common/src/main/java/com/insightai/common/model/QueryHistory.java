package com.insightai.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("query_history")
public class QueryHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dataSourceId;
    private String queryText;
    private String normalizedQuery; // AI normalized for caching
    private String executePlan;
    private Long executionTimeMs;
    private Integer resultCount;
    private String status; // SUCCESS, FAILED, PARTIAL
    private String errorMessage;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
