package com.insightai.service;

import com.insightai.common.dto.QueryRequest;
import com.insightai.common.dto.QueryResponse;
import com.insightai.common.model.QueryCache;
import com.insightai.common.model.QueryHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Query Execution Service
 * Handles natural language and SQL query execution with caching
 */
@Slf4j
@Service
public class QueryService {

    // In-memory cache for demo purposes - use Redis in production
    private final Map<String, QueryResponse> queryCache = new ConcurrentHashMap<>();
    
    // Query history storage
    private final Map<String, QueryHistory> queryHistoryMap = new ConcurrentHashMap<>();

    /**
     * Execute a query (natural language or SQL)
     */
    public QueryResponse executeQuery(QueryRequest request) {
        log.info("Executing query in {} mode", request.getMode());
        long startTime = System.currentTimeMillis();
        
        String queryId = UUID.randomUUID().toString();
        
        // Check cache if enabled
        if (request.isUseCache()) {
            QueryResponse cached = getCachedResponse(request.getQuery());
            if (cached != null) {
                log.info("Cache hit for query: {}", request.getQuery());
                cached.setCacheStatus("HIT");
                return cached;
            }
        }
        
        // Process based on mode
        QueryResponse response;
        switch (request.getMode()) {
            case NATURAL_LANGUAGE:
                response = processNaturalLanguageQuery(request);
                break;
            case SQL:
                response = executeSqlQuery(request);
                break;
            case PARAMETERIZED:
                response = executeParameterizedQuery(request);
                break;
            default:
                response = executeSqlQuery(request);
        }
        
        response.setQueryId(queryId);
        response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        response.setCacheStatus("MISS");
        
        // Cache the result
        if (request.isUseCache()) {
            cacheResponse(request.getQuery(), response);
        }
        
        // Record history
        recordQueryHistory(request, response);
        
        return response;
    }

    /**
     * Get query result by ID
     */
    public Optional<QueryResponse> getQueryResult(String queryId) {
        log.info("Fetching query result for ID: {}", queryId);
        // In a real implementation, this would query the repository
        return Optional.empty();
    }

    /**
     * Get query history for a data source
     */
    public List<QueryHistory> getQueryHistory(Long dataSourceId, int limit) {
        log.info("Fetching query history for data source: {}, limit: {}", dataSourceId, limit);
        // In a real implementation, this would query the repository
        return List.of();
    }

    /**
     * Clear query cache
     */
    public void clearCache() {
        log.info("Clearing query cache");
        queryCache.clear();
    }

    /**
     * Clear cache for specific query
     */
    public void clearCacheForQuery(String query) {
        log.info("Clearing cache for query: {}", query);
        queryCache.remove(query);
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "size", queryCache.size(),
            "hits", queryCache.values().stream().filter(r -> "HIT".equals(r.getCacheStatus())).count(),
            "misses", queryCache.values().stream().filter(r -> "MISS".equals(r.getCacheStatus())).count()
        );
    }

    private QueryResponse processNaturalLanguageQuery(QueryRequest request) {
        log.info("Processing natural language query: {}", request.getQuery());
        // In a real implementation, this would call the AI service to convert NL to SQL
        // then execute the generated SQL
        return QueryResponse.builder()
                .executedQuery("-- Natural language query processed")
                .data(List.of())
                .status("SUCCESS")
                .rowCount(0)
                .columns(List.of())
                .build();
    }

    private QueryResponse executeSqlQuery(QueryRequest request) {
        log.info("Executing SQL query: {}", request.getQuery());
        // In a real implementation, this would execute the SQL against the data source
        return QueryResponse.builder()
                .executedQuery(request.getQuery())
                .data(List.of())
                .status("SUCCESS")
                .rowCount(0)
                .columns(List.of())
                .build();
    }

    private QueryResponse executeParameterizedQuery(QueryRequest request) {
        log.info("Executing parameterized query: {}", request.getQuery());
        // In a real implementation, this would execute the parameterized SQL
        return QueryResponse.builder()
                .executedQuery(request.getQuery())
                .data(List.of())
                .status("SUCCESS")
                .rowCount(0)
                .columns(List.of())
                .build();
    }

    private QueryResponse getCachedResponse(String query) {
        return queryCache.get(query);
    }

    private void cacheResponse(String query, QueryResponse response) {
        queryCache.put(query, response);
    }

    private void recordQueryHistory(QueryRequest request, QueryResponse response) {
        QueryHistory history = QueryHistory.builder()
                .id(System.currentTimeMillis())
                .dataSourceId(request.getDataSourceId())
                .queryText(request.getQuery())
                .normalizedQuery(request.getQuery().toLowerCase().trim())
                .executePlan(response.getExecutedQuery())
                .executionTimeMs(response.getExecutionTimeMs())
                .resultCount(response.getRowCount())
                .status(response.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
        queryHistoryMap.put(response.getQueryId(), history);
    }
}