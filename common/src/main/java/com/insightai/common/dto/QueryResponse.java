package com.insightai.common.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    private String queryId;
    private String executedQuery;
    private List<Map<String, Object>> data;
    private List<ColumnMetadata> columns;
    private long executionTimeMs;
    private int rowCount;
    private String status;
    private String cacheStatus; // HIT, MISS, BYPASS
    private QueryOptimization optimization;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnMetadata {
        private String name;
        private String type;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryOptimization {
        private boolean wasOptimized;
        private String originalQuery;
        private String optimizedQuery;
        private String aiModel;
        private Double confidence;
    }
}
