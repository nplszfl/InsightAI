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
    private String originalQuery;
    private String generatedSql;
    private String intent;
    private String queryStatus;
    private String errorMessage;
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