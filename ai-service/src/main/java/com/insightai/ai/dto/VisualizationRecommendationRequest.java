package com.insightai.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationRecommendationRequest {
    @NotEmpty(message = "Column definitions cannot be empty")
    private List<ColumnDefinition> columns;
    private String queryContext;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnDefinition {
        private String name;
        private String dataType;
        private String role;
    }
}