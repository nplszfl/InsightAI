package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import com.insightai.nlquery.entity.ConversationContext;
import com.insightai.nlquery.mapper.ConversationContextMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence facade for {@link ConversationContext}.
 *
 * <p>Used by the controller layer to:
 * <ul>
 *     <li>Save a freshly processed turn so subsequent turns can reference it.</li>
 *     <li>Load the most recent N turns for a session (oldest-first) for
 *         coreference resolution in {@link NL2SQLService}.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryHistoryService {

    private final ConversationContextMapper conversationContextMapper;

    /** Save a turn; returns the persisted id (or null if insert was a no-op). */
    @Transactional
    public Long saveContext(ConversationContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("ConversationContext must not be null");
        }
        if (ctx.getCreatedAt() == null) {
            ctx.setCreatedAt(LocalDateTime.now());
        }
        if (ctx.getExecuted() == null) {
            ctx.setExecuted(Boolean.FALSE);
        }
        conversationContextMapper.insert(ctx);
        log.debug("Saved conversation context id={} session={}", ctx.getId(), ctx.getSessionId());
        return ctx.getId();
    }

    /** Return the most recent {@code limit} turns for a session, oldest-first. */
    public List<ConversationContextDto> getRecentBySession(String sessionId, int limit) {
        if (sessionId == null || sessionId.isBlank() || limit <= 0) {
            return Collections.emptyList();
        }
        List<ConversationContext> rows =
                conversationContextMapper.selectRecentBySession(sessionId, limit);
        // mapper returns newest-first; reverse for natural chronological consumption
        List<ConversationContextDto> dtos = rows.stream()
                .map(ConversationContextDto::fromEntity)
                .collect(Collectors.toList());
        Collections.reverse(dtos);
        return dtos;
    }
}
