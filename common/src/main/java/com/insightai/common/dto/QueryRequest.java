package com.insightai.common.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    private Long dataSourceId;
    private String query; // Natural language or SQL
    private Map<String, Object> parameters;
    private QueryMode mode;
    private boolean useCache;
    private boolean optimizeWithAI;

    public enum QueryMode {
        SQL,              // Direct SQL execution
        NATURAL_LANGUAGE, // AI interprets natural language
        PARAMETERIZED     // With named parameters
    }
}
