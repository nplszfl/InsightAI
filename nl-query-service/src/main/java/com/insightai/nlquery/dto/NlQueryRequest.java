package com.insightai.nlquery.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlQueryRequest {
    @NotBlank(message = "Query text is required")
    private String queryText;
    
    private String dataSource;
    
    private List<String> tableNames;
    
    private Map<String, String> columnMappings;
    
    private String createdBy;
}