package com.insightai.service;

import com.insightai.common.dto.QueryRequest;
import com.insightai.common.dto.QueryRequest.QueryMode;
import com.insightai.common.dto.QueryResponse;
import com.insightai.common.model.QueryHistory;
import com.insightai.repository.QueryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QueryService.
 * Tests query execution, caching, and history management.
 */
@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    @Mock
    private QueryHistoryRepository queryHistoryRepository;

    private QueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new QueryService(queryHistoryRepository);
    }

    @Test
    void executeQuery_inSqlMode_setsCorrectStatus() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("SELECT * FROM users");
        request.setMode(QueryMode.SQL);
        request.setDataSourceId(1L);
        request.setUseCache(false);

        when(queryHistoryRepository.insert(any(QueryHistory.class))).thenReturn(1);

        // Act
        QueryResponse response = queryService.executeQuery(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getExecutedQuery()).isEqualTo("SELECT * FROM users");
        assertThat(response.getCacheStatus()).isEqualTo("MISS");
    }

    @Test
    void executeQuery_inNaturalLanguageMode_processesQuery() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("Show me all users who signed up this month");
        request.setMode(QueryMode.NATURAL_LANGUAGE);
        request.setDataSourceId(1L);
        request.setUseCache(false);

        when(queryHistoryRepository.insert(any(QueryHistory.class))).thenReturn(1);

        // Act
        QueryResponse response = queryService.executeQuery(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getExecutedQuery()).contains("Natural language query processed");
    }

    @Test
    void executeQuery_generatesUniqueQueryId() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("SELECT 1");
        request.setMode(QueryMode.SQL);
        request.setDataSourceId(1L);
        request.setUseCache(false);

        when(queryHistoryRepository.insert(any(QueryHistory.class))).thenReturn(1);

        // Act
        QueryResponse response1 = queryService.executeQuery(request);
        QueryResponse response2 = queryService.executeQuery(request);

        // Assert
        assertThat(response1.getQueryId()).isNotNull();
        assertThat(response2.getQueryId()).isNotNull();
        assertThat(response1.getQueryId()).isNotEqualTo(response2.getQueryId());
    }

    @Test
    void executeQuery_recordsExecutionTime() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("SELECT 1");
        request.setMode(QueryMode.SQL);
        request.setDataSourceId(1L);
        request.setUseCache(false);

        when(queryHistoryRepository.insert(any(QueryHistory.class))).thenReturn(1);

        // Act
        QueryResponse response = queryService.executeQuery(request);

        // Assert
        assertThat(response.getExecutionTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void executeQuery_recordsQueryHistory() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("SELECT * FROM orders");
        request.setMode(QueryMode.SQL);
        request.setDataSourceId(1L);
        request.setUseCache(false);

        when(queryHistoryRepository.insert(any(QueryHistory.class))).thenReturn(1);

        // Act
        queryService.executeQuery(request);

        // Assert
        verify(queryHistoryRepository).insert(any(QueryHistory.class));
    }

    @Test
    void getQueryHistory_returnsLimitedResults() {
        // Arrange
        Long dataSourceId = 1L;
        QueryHistory history1 = new QueryHistory();
        history1.setQueryText("SELECT 1");
        
        QueryHistory history2 = new QueryHistory();
        history2.setQueryText("SELECT 2");

        when(queryHistoryRepository.findByDataSourceIdOrderByCreatedAtDesc(dataSourceId))
                .thenReturn(List.of(history1, history2));

        // Act
        List<QueryHistory> result = queryService.getQueryHistory(dataSourceId, 10);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void executeQuery_withParameterizedMode_executesParameterizedQuery() {
        // Arrange
        QueryRequest request = new QueryRequest();
        request.setQuery("SELECT * FROM users WHERE id = ?");
        request.setMode(QueryMode.PARAMETERIZED);
        request.setDataSourceId(1L);
        request.setUseCache(false);

        when(queryHistoryRepository.insert(any(QueryHistory.class))).thenReturn(1);

        // Act
        QueryResponse response = queryService.executeQuery(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getExecutedQuery()).isEqualTo("SELECT * FROM users WHERE id = ?");
    }
}