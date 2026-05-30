package com.insightai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.insightai.common.dto.QueryRequest;
import com.insightai.common.dto.QueryResponse;
import com.insightai.common.model.QueryCache;
import com.insightai.common.model.QueryHistory;
import com.insightai.repository.QueryCacheRepository;
import com.insightai.repository.QueryHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Query Execution Service
 * Handles natural language and SQL query execution with MyBatis Plus persistence
 */
@Slf4j
@Service
public class QueryService extends ServiceImpl<QueryCacheRepository, QueryCache> {

    private final QueryHistoryRepository queryHistoryRepository;
    private final Map<String, QueryResponse> memoryCache = new ConcurrentHashMap<>();

    public QueryService(QueryHistoryRepository queryHistoryRepository) {
        this.queryHistoryRepository = queryHistoryRepository;
    }

    /**
     * Execute a query (natural language or SQL)
     */
    public QueryResponse executeQuery(QueryRequest request) {
        log.info("Executing query in {} mode", request.getMode());
        long startTime = System.currentTimeMillis();

        String queryId = java.util.UUID.randomUUID().toString();

        // Check cache if enabled
        if (request.isUseCache()) {
            String cacheKey = generateCacheKey(request);
            QueryResponse cached = getCachedResponse(cacheKey);
            if (cached != null) {
                log.info("Cache hit for query: {}", request.getQuery());
                cached.setQueryId(queryId);
                cached.setCacheStatus("HIT");
                cached.setExecutionTimeMs(System.currentTimeMillis() - startTime);
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

        // Cache the result in memory + DB
        if (request.isUseCache()) {
            String cacheKey = generateCacheKey(request);
            cacheResponse(cacheKey, response);
        }

        // Record history in database
        recordQueryHistory(request, response);

        return response;
    }

    /**
     * Get query result by ID from history
     */
    public Optional<QueryResponse> getQueryResult(String queryId) {
        log.info("Fetching query result for ID: {}", queryId);
        QueryHistory history = queryHistoryRepository.selectById(queryId);
        if (history == null) {
            return Optional.empty();
        }
        return Optional.of(QueryResponse.builder()
                .queryId(String.valueOf(history.getId()))
                .executedQuery(history.getExecutePlan())
                .executionTimeMs(history.getExecutionTimeMs())
                .rowCount(history.getResultCount())
                .status(history.getStatus())
                .cacheStatus("HIT")
                .build());
    }

    /**
     * Get query history for a data source
     */
    public List<QueryHistory> getQueryHistory(Long dataSourceId, int limit) {
        log.info("Fetching query history for data source: {}, limit: {}", dataSourceId, limit);
        return queryHistoryRepository.findByDataSourceIdOrderByCreatedAtDesc(dataSourceId)
                .stream().limit(limit).toList();
    }

    /**
     * Clear query cache (both memory and DB)
     */
    public void clearCache() {
        log.info("Clearing query cache");
        memoryCache.clear();
        // Delete old expired cache entries from DB
        lambdaUpdate().remove();
    }

    /**
     * Clear cache for specific query
     */
    public void clearCacheForQuery(String query) {
        log.info("Clearing cache for query: {}", query);
        String cacheKey = query.toLowerCase().trim();
        memoryCache.remove(cacheKey);
        lambdaUpdate()
                .eq(QueryCache::getCacheKey, cacheKey)
                .remove();
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        long total = this.count();
        long memoryHits = memoryCache.values().stream()
                .filter(r -> "HIT".equals(r.getCacheStatus())).count();
        long memoryMisses = memoryCache.values().stream()
                .filter(r -> "MISS".equals(r.getCacheStatus())).count();
        return Map.of(
            "dbCacheSize", total,
            "memoryCacheSize", memoryCache.size(),
            "memoryHits", memoryHits,
            "memoryMisses", memoryMisses
        );
    }

    /**
     * Delete query history by ID
     */
    public boolean deleteQueryHistory(Long id) {
        log.info("Deleting query history: {}", id);
        return queryHistoryRepository.deleteById(id) > 0;
    }

    /**
     * Clear query history for a data source
     */
    public int clearHistoryForDataSource(Long dataSourceId) {
        log.info("Clearing query history for data source: {}", dataSourceId);
        LambdaQueryWrapper<QueryHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueryHistory::getDataSourceId, dataSourceId);
        return queryHistoryRepository.delete(wrapper);
    }

    /**
     * Get failed queries for debugging
     */
    public List<QueryHistory> getFailedQueries(Long dataSourceId, int limit) {
        log.info("Fetching failed queries for data source: {}", dataSourceId);
        return queryHistoryRepository.findByDataSourceIdOrderByCreatedAtDesc(dataSourceId)
                .stream()
                .filter(h -> "FAILED".equals(h.getStatus()))
                .limit(limit)
                .toList();
    }

    /**
     * Get query statistics for a data source
     */
    public Map<String, Object> getQueryStats(Long dataSourceId) {
        List<QueryHistory> history = queryHistoryRepository.findByDataSourceIdOrderByCreatedAtDesc(dataSourceId);
        long total = history.size();
        long success = history.stream().filter(h -> "SUCCESS".equals(h.getStatus())).count();
        long failed = history.stream().filter(h -> "FAILED".equals(h.getStatus())).count();
        double avgTime = history.stream()
                .filter(h -> h.getExecutionTimeMs() != null)
                .mapToLong(QueryHistory::getExecutionTimeMs)
                .average()
                .orElse(0.0);
        return Map.of(
            "totalQueries", total,
            "successful", success,
            "failed", failed,
            "averageExecutionTimeMs", avgTime
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

    private String generateCacheKey(QueryRequest request) {
        try {
            String input = (request.getQuery() + request.getDataSourceId() + request.getMode()).toLowerCase().trim();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return request.getQuery().toLowerCase().trim();
        }
    }

    private QueryResponse getCachedResponse(String cacheKey) {
        // First check memory cache
        QueryResponse memoryCached = memoryCache.get(cacheKey);
        if (memoryCached != null) {
            return memoryCached;
        }
        // Then check DB cache
        QueryCache dbCached = this.lambdaQuery()
                .eq(QueryCache::getCacheKey, cacheKey)
                .one();
        if (dbCached != null) {
            // Increment hits
            dbCached.setHits(dbCached.getHits() + 1);
            this.updateById(dbCached);
            // Parse cached result back to response
            QueryResponse response = QueryResponse.builder()
                    .executedQuery(dbCached.getQuerySignature())
                    .status("SUCCESS")
                    .cacheStatus("HIT")
                    .build();
            memoryCache.put(cacheKey, response);
            return response;
        }
        return null;
    }

    private void cacheResponse(String cacheKey, QueryResponse response) {
        // Store in memory cache
        memoryCache.put(cacheKey, response);
        // Store in DB cache
        QueryCache cache = QueryCache.builder()
                .cacheKey(cacheKey)
                .querySignature(response.getExecutedQuery())
                .resultSchema("{}")
                .cachedResult("{}")
                .hits(0L)
                .ttlSeconds(3600L)
                .freshnessLevel("REAL_TIME")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        this.save(cache);
    }

    private void recordQueryHistory(QueryRequest request, QueryResponse response) {
        QueryHistory history = QueryHistory.builder()
                .dataSourceId(request.getDataSourceId())
                .queryText(request.getQuery())
                .normalizedQuery(request.getQuery().toLowerCase().trim())
                .executePlan(response.getExecutedQuery())
                .executionTimeMs(response.getExecutionTimeMs())
                .resultCount(response.getRowCount())
                .status(response.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
        queryHistoryRepository.insert(history);
    }
}
