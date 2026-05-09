package com.insightai.common.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private String name;
    private String type;
    private String config;
    private String filters;
    private Long createdBy;
    private List<VisualizationDto> visualizations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualizationDto {
        private Long id;
        private String chartType;
        private String title;
        private String queryId;
        private String config;
        private Integer position;
    }
}
