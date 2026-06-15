package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import com.insightai.nlquery.entity.ConversationContext;
import com.insightai.nlquery.mapper.ConversationContextMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link QueryHistoryService}.
 */
@ExtendWith(MockitoExtension.class)
class QueryHistoryServiceTest {

    @Mock
    private ConversationContextMapper mapper;

    @InjectMocks
    private QueryHistoryService service;

    private ConversationContext sample;

    @BeforeEach
    void setUp() {
        sample = ConversationContext.builder()
                .sessionId("sess-A")
                .userId("u1")
                .queryText("top sales")
                .generatedSql("SELECT * FROM sales LIMIT 10")
                .tableName("sales")
                .intent("RANKING")
                .build();
    }

    @Test
    @DisplayName("saveContext 写入 Mapper 并设置默认 executed=false")
    void testSaveContextSetsDefaults() {
        when(mapper.insert(any(ConversationContext.class))).thenAnswer(inv -> {
            ConversationContext c = inv.getArgument(0);
            c.setId(42L);
            return 1;
        });

        Long id = service.saveContext(sample);

        ArgumentCaptor<ConversationContext> captor = ArgumentCaptor.forClass(ConversationContext.class);
        verify(mapper, times(1)).insert(captor.capture());
        ConversationContext saved = captor.getValue();
        assertEquals(42L, id);
        assertNotNull(saved.getCreatedAt(), "createdAt must be auto-populated");
        assertEquals(Boolean.FALSE, saved.getExecuted(), "executed must default to false");
    }

    @Test
    @DisplayName("getRecentBySession 按 sessionId+limit 委托给 mapper 并按时间正序返回")
    void testGetRecentBySessionMapsAndReverses() {
        ConversationContext newer = ConversationContext.builder()
                .sessionId("sess-A").tableName("orders").id(2L).build();
        ConversationContext older = ConversationContext.builder()
                .sessionId("sess-A").tableName("sales").id(1L).build();
        // mapper returns newest-first
        when(mapper.selectRecentBySession(eq("sess-A"), anyInt()))
                .thenReturn(List.of(newer, older));

        List<ConversationContextDto> dtos = service.getRecentBySession("sess-A", 10);

        assertEquals(2, dtos.size());
        // service must reverse so consumers see chronological order
        assertEquals("sales", dtos.get(0).getTableName());
        assertEquals("orders", dtos.get(1).getTableName());
        verify(mapper).selectRecentBySession("sess-A", 10);
    }

    @Test
    @DisplayName("getRecentBySession 对非法输入返回空列表")
    void testGetRecentBySessionGuardsInvalidInput() {
        assertTrue(service.getRecentBySession(null, 5).isEmpty());
        assertTrue(service.getRecentBySession("", 5).isEmpty());
        assertTrue(service.getRecentBySession("sess", 0).isEmpty());
        assertTrue(service.getRecentBySession("sess", -1).isEmpty());
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("saveContext 拒绝 null 输入")
    void testSaveContextRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.saveContext(null));
    }
}
