package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.NlQueryRequest;
import com.insightai.nlquery.dto.NlQueryResponse;
import com.insightai.nlquery.entity.NlQueryHistory;
import com.insightai.nlquery.entity.VisualizationResult;
import com.insightai.nlquery.mapper.NlQueryHistoryMapper;
import com.insightai.nlquery.mapper.VisualizationResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NlQueryService {
    
    private final NlQueryHistoryMapper nlQueryHistoryMapper;
    private final VisualizationResultMapper visualizationResultMapper;
    
    @Transactional
    public NlQueryResponse processQuery(NlQueryRequest request) {
        log.info("Processing natural language query: {}", request.getQueryText());
        
        String intent = interpretIntent(request.getQueryText());
        String sql = generateSql(request.getQueryText(), intent, request.getTableNames(), request.getColumnMappings());
        
        NlQueryHistory history = NlQueryHistory.builder()
                .originalQuery(request.getQueryText())
                .generatedSql(sql)
                .intent(intent)
                .dataSource(request.getDataSource())
                .tableName(request.getTableNames() != null && !request.getTableNames().isEmpty() ? 
                        String.join(",", request.getTableNames()) : null)
                .queryStatus("GENERATED")
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        nlQueryHistoryMapper.insert(history);
        
        VisualizationResult visualization = createVisualization(history.getId(), sql, intent);
        
        return NlQueryResponse.builder()
                .id(history.getId())
                .originalQuery(request.getQueryText())
                .generatedSql(sql)
                .intent(intent)
                .queryStatus(history.getQueryStatus())
                .visualization(buildVisualizationData(visualization, intent))
                .timestamp(history.getCreatedAt())
                .build();
    }
    
    private String interpretIntent(String queryText) {
        String lowerQuery = queryText.toLowerCase();
        
        if (lowerQuery.contains("total") || lowerQuery.contains("sum") || lowerQuery.contains("count")) {
            return "AGGREGATION";
        }
        if (lowerQuery.contains("trend") || lowerQuery.contains("over") || lowerQuery.contains("change")) {
            return "TREND_ANALYSIS";
        }
        if (lowerQuery.contains("compare") || lowerQuery.contains("difference") || lowerQuery.contains("versus")) {
            return "COMPARISON";
        }
        if (lowerQuery.contains("top") || lowerQuery.contains("highest") || lowerQuery.contains("lowest")) {
            return "RANKING";
        }
        if (lowerQuery.contains("average") || lowerQuery.contains("mean")) {
            return "AVERAGE";
        }
        if (lowerQuery.contains("percentage") || lowerQuery.contains("percent")) {
            return "PERCENTAGE";
        }
        
        return "GENERAL_QUERY";
    }
    
    private String generateSql(String queryText, String intent, List<String> tableNames, 
                               Map<String, String> columnMappings) {
        String table = (tableNames != null && !tableNames.isEmpty()) ? tableNames.get(0) : "data_table";
        String columns = "*";
        
        if (columnMappings != null && !columnMappings.isEmpty()) {
            columns = String.join(", ", columnMappings.values());
        }
        
        StringBuilder sql = new StringBuilder("SELECT ");
        
        switch (intent) {
            case "AGGREGATION":
                sql.append("COUNT(*) as total, SUM(value) as sum_value ");
                break;
            case "AVERAGE":
                sql.append("AVG(value) as average_value ");
                break;
            case "TREND_ANALYSIS":
                sql.append("date, value ");
                break;
            case "COMPARISON":
                sql.append("category, value ");
                break;
            case "RANKING":
                sql.append("TOP 10 * ");
                break;
            default:
                sql.append(columns).append(" ");
        }
        
        sql.append("FROM ").append(table);
        
        String lowerQuery = queryText.toLowerCase();
        if (lowerQuery.contains("where")) {
            sql.append(" WHERE 1=1");
        }
        if (lowerQuery.contains("group by")) {
            sql.append(" GROUP BY category");
        }
        if (lowerQuery.contains("order by")) {
            sql.append(" ORDER BY value DESC");
        }
        
        return sql.toString();
    }
    
    private VisualizationResult createVisualization(Long queryHistoryId, String sql, String intent) {
        String chartType = determineChartType(intent);
        
        VisualizationResult visualization = VisualizationResult.builder()
                .queryHistoryId(queryHistoryId)
                .chartType(chartType)
                .chartConfig(generateChartConfig(chartType))
                .dataSummary(String.format("Data visualized as %s chart", chartType))
                .createdBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        visualizationResultMapper.insert(visualization);
        return visualization;
    }
    
    private String determineChartType(String intent) {
        switch (intent) {
            case "AGGREGATION":
            case "AVERAGE":
                return "BAR";
            case "TREND_ANALYSIS":
                return "LINE";
            case "COMPARISON":
                return "PIE";
            case "RANKING":
                return "HORIZONTAL_BAR";
            default:
                return "TABLE";
        }
    }
    
    private String generateChartConfig(String chartType) {
        Map<String, Object> config = new HashMap<>();
        config.put("type", chartType);
        config.put("showLegend", true);
        config.put("animate", true);
        
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config);
    }
    
    private NlQueryResponse.VisualizationData buildVisualizationData(VisualizationResult visualization, String intent) {
        List<Map<String, Object>> sampleData = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("label", "Item " + i);
            row.put("value", Math.random() * 100);
            sampleData.add(row);
        }
        
        return NlQueryResponse.VisualizationData.builder()
                .chartType(visualization.getChartType())
                .data(sampleData)
                .summary(visualization.getDataSummary())
                .build();
    }
}