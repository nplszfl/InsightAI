package com.insightai.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("query_cache")
public class QueryCache {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cacheKey; // Hash of normalized query + params
    private String querySignature;
    private String resultSchema; // JSON schema of cached result
    private String cachedResult; // JSON result data
    private Long hits;
    private Long ttlSeconds;
    private String freshnessLevel; // REAL_TIME, NEAR_REAL_TIME, STATIC
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
