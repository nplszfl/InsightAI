package com.insightai.nlquery.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlQueryResponse {
    private Long id;
    private String sessionId;
    private String originalQuery;
    /** Query after coreference resolution has been applied. */
    private String resolvedQuery;
    private String generatedSql;
    private String intent;
    private String queryStatus;
    private String errorMessage;
    private Boolean sqlSafe;
    /** When SQL is rejected this carries the validator's reason. */
    private List<String> sqlViolations;
    private VisualizationData visualization;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualizationData {
        private String chartType;
        private List<Map<String, Object>> data;
        private String summary;
    }
}
