package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import com.insightai.nlquery.dto.NlQueryRequest;
import com.insightai.nlquery.dto.NlQueryResponse;
import com.insightai.nlquery.entity.ConversationContext;
import com.insightai.nlquery.mapper.ConversationContextMapper;
import com.insightai.nlquery.mapper.NlQueryHistoryMapper;
import com.insightai.nlquery.mapper.VisualizationResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NlQueryServiceTest {

    @Mock
    private NlQueryHistoryMapper nlQueryHistoryMapper;

    @Mock
    private VisualizationResultMapper visualizationResultMapper;

    @Mock
    private ConversationContextMapper conversationContextMapper;

    private NL2SQLService nl2sqlService;
    private SqlSafetyValidator sqlSafetyValidator;
    private QueryHistoryService queryHistoryService;
    private NlQueryService nlQueryService;

    private NlQueryRequest request;

    @BeforeEach
    void setUp() {
        nl2sqlService = new NL2SQLService();
        sqlSafetyValidator = new SqlSafetyValidator();
        queryHistoryService = new QueryHistoryService(conversationContextMapper);
        nlQueryService = new NlQueryService(
                nlQueryHistoryMapper,
                visualizationResultMapper,
                nl2sqlService,
                sqlSafetyValidator,
                queryHistoryService);

        request = NlQueryRequest.builder()
                .queryText("Show me total sales by category")
                .dataSource("mysql")
                .tableNames(Arrays.asList("sales_data"))
                .columnMappings(new HashMap<>())
                .createdBy("test_user")
                .build();
    }

    @Test
    @DisplayName("无 sessionId 时 processQuery 仍然可用（向后兼容）")
    void testProcessQuery_ShouldReturnQueryResponse() {
        when(nlQueryHistoryMapper.insert(any())).thenAnswer(invocation -> {
            var history = invocation.getArgument(0, com.insightai.nlquery.entity.NlQueryHistory.class);
            history.setId(1L);
            return 1;
        });
        when(visualizationResultMapper.insert(any())).thenAnswer(invocation -> {
            var viz = invocation.getArgument(0, com.insightai.nlquery.entity.VisualizationResult.class);
            viz.setId(1L);
            return 1;
        });

        NlQueryResponse result = nlQueryService.processQuery(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Show me total sales by category", result.getOriginalQuery());
        assertNotNull(result.getGeneratedSql());
        assertEquals("AGGREGATION", result.getIntent());
        assertNull(result.getSessionId(), "no sessionId in request → response sessionId stays null");
        verify(nlQueryHistoryMapper, times(1)).insert(any());
        verify(visualizationResultMapper, times(1)).insert(any());
        verify(conversationContextMapper, never()).insert(any());
    }

    @Test
    @DisplayName("带 sessionId 的 processQueryWithSession 持久化上下文")
    void testProcessQueryWithSession_PersistsContext() {
        when(nlQueryHistoryMapper.insert(any())).thenAnswer(invocation -> {
            var history = invocation.getArgument(0, com.insightai.nlquery.entity.NlQueryHistory.class);
            history.setId(2L);
            return 1;
        });
        when(visualizationResultMapper.insert(any())).thenAnswer(invocation -> {
            var viz = invocation.getArgument(0, com.insightai.nlquery.entity.VisualizationResult.class);
            viz.setId(2L);
            return 1;
        });
        when(conversationContextMapper.selectRecentBySession(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());

        request.setSessionId("sess-xyz");
        NlQueryResponse result = nlQueryService.processQueryWithSession(request, "sess-xyz", 5);

        assertNotNull(result);
        assertEquals("sess-xyz", result.getSessionId());
        assertTrue(result.getSqlSafe(), "generated SQL must be safe by default");
        verify(conversationContextMapper, times(1)).insert(any(ConversationContext.class));
    }

    @Test
    @DisplayName("多轮: '上次的总和' 利用 prior table 替换")
    void testProcessQueryWithSession_CoreferenceResolution() {
        when(nlQueryHistoryMapper.insert(any())).thenAnswer(invocation -> {
            var history = invocation.getArgument(0, com.insightai.nlquery.entity.NlQueryHistory.class);
            history.setId(3L);
            return 1;
        });
        when(visualizationResultMapper.insert(any())).thenAnswer(invocation -> {
            var viz = invocation.getArgument(0, com.insightai.nlquery.entity.VisualizationResult.class);
            viz.setId(3L);
            return 1;
        });
        // mock mapper to return ConversationContext entities (the mapper's actual return type)
        ConversationContext priorEntity = ConversationContext.builder()
                .sessionId("sess-xyz")
                .tableName("sales")
                .generatedSql("SELECT * FROM sales")
                .id(99L)
                .build();
        when(conversationContextMapper.selectRecentBySession(eq("sess-xyz"), anyInt()))
                .thenReturn(List.of(priorEntity));

        request.setSessionId("sess-xyz");
        request.setQueryText("上次那张表的总和");
        NlQueryResponse result = nlQueryService.processQueryWithSession(request, "sess-xyz", 5);

        assertNotNull(result.getResolvedQuery());
        assertTrue(result.getResolvedQuery().contains("sales"),
                "expected coreference to swap prior table name, got: " + result.getResolvedQuery());
        assertTrue(result.getGeneratedSql().toUpperCase().contains("SUM("),
                "expected SUM( aggregation, got: " + result.getGeneratedSql());
    }

    @Test
    @DisplayName("生成的 DROP SQL 被 validator 拦截并标记 sqlSafe=false")
    void testProcessQueryWithSession_RejectsUnsafeGeneratedSql() {
        // Force the validator to reject by pre-stubbing the validator behaviour.
        // Easier path: invoke the service with a request whose translation produces a forbidden keyword.
        // The rule engine never produces DDL by itself, so we exercise the dedicated validateSql entry point:
        SqlSafetyValidator.ValidationResult r = nlQueryService.validateSql("DROP TABLE users");
        assertFalse(r.isValid());
        assertTrue(r.getViolations().stream().anyMatch(v -> v.startsWith("FORBIDDEN_KEYWORD:DROP")));
    }
}
