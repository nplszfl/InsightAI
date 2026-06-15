package com.insightai.nlquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Conversation context entity - stores per-session multi-turn dialogue history
 * to enable coreference/anaphora resolution in NL2SQL (e.g. "上次的表" → real table).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation_context")
public class ConversationContext {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Session identifier grouping multiple turns of the same dialogue */
    private String sessionId;

    /** User identifier owning this turn */
    private String userId;

    /** Original natural language query as supplied by the user */
    private String queryText;

    /** SQL generated from queryText (null if generation failed) */
    private String generatedSql;

    /** Detected intent / table name for downstream reference resolution */
    private String intent;
    private String tableName;

    /** Whether the generated SQL was actually executed */
    private Boolean executed;

    /** Row count returned by execution (nullable) */
    private Integer rowCount;

    /** Error message if SQL execution failed (nullable) */
    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
