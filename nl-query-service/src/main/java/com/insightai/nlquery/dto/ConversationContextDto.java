package com.insightai.nlquery.dto;

import com.insightai.nlquery.entity.ConversationContext;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContextDto {
    private Long id;
    private String sessionId;
    private String userId;
    private String queryText;
    private String generatedSql;
    private String intent;
    private String tableName;
    private Boolean executed;
    private Integer rowCount;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static ConversationContextDto fromEntity(ConversationContext ctx) {
        if (ctx == null) return null;
        return ConversationContextDto.builder()
                .id(ctx.getId())
                .sessionId(ctx.getSessionId())
                .userId(ctx.getUserId())
                .queryText(ctx.getQueryText())
                .generatedSql(ctx.getGeneratedSql())
                .intent(ctx.getIntent())
                .tableName(ctx.getTableName())
                .executed(ctx.getExecuted())
                .rowCount(ctx.getRowCount())
                .errorMessage(ctx.getErrorMessage())
                .createdAt(ctx.getCreatedAt())
                .build();
    }
}
