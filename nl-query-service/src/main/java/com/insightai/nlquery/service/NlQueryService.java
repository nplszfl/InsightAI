package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import com.insightai.nlquery.dto.NlQueryRequest;
import com.insightai.nlquery.dto.NlQueryResponse;
import com.insightai.nlquery.entity.ConversationContext;
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
    private final NL2SQLService nl2sqlService;
    private final SqlSafetyValidator sqlSafetyValidator;
    private final QueryHistoryService queryHistoryService;

    // ---------- legacy single-turn entry point (kept for backwards compatibility) ----------

    @Transactional
    public NlQueryResponse processQuery(NlQueryRequest request) {
        return processQueryWithSession(request, null, 0);
    }

    // ---------- multi-turn aware entry point ----------

    /**
     * Process a query with full session / context awareness.
     *
     * @param request              request payload
     * @param sessionId            explicit session id (may be null for stateless call)
     * @param contextLookbackLimit how many prior turns to consider for coreference resolution
     */
    @Transactional
    public NlQueryResponse processQueryWithSession(NlQueryRequest request,
                                                    String sessionId,
                                                    int contextLookbackLimit) {
        log.info("Processing natural language query: '{}' (session={})",
                request.getQueryText(), sessionId);

        String effectiveSessionId = sessionId != null && !sessionId.isBlank()
                ? sessionId
                : request.getSessionId();

        List<ConversationContextDto> priorContext = (effectiveSessionId != null)
                ? queryHistoryService.getRecentBySession(effectiveSessionId,
                        contextLookbackLimit > 0 ? contextLookbackLimit : 5)
                : Collections.emptyList();

        String resolved = nl2sqlService.resolveReferences(request.getQueryText(), priorContext);
        String intent = interpretIntent(resolved);
        String sql = nl2sqlService.generateSql(resolved);

        SqlSafetyValidator.ValidationResult safety = sqlSafetyValidator.validate(sql);
        boolean safe = safety.isValid();
        String status = safe ? "GENERATED" : "REJECTED";

        NlQueryHistory history = NlQueryHistory.builder()
                .originalQuery(request.getQueryText())
                .generatedSql(sql)
                .intent(intent)
                .dataSource(request.getDataSource())
                .tableName(extractTableName(sql))
                .queryStatus(status)
                .errorMessage(safe ? null : safety.getReason())
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        nlQueryHistoryMapper.insert(history);

        VisualizationResult visualization = null;
        if (safe) {
            visualization = createVisualization(history.getId(), sql, intent);
        }

        // Persist conversation context for subsequent turns (only if we have a session)
        if (effectiveSessionId != null) {
            ConversationContext ctx = ConversationContext.builder()
                    .sessionId(effectiveSessionId)
                    .userId(request.getCreatedBy())
                    .queryText(request.getQueryText())
                    .generatedSql(sql)
                    .intent(intent)
                    .tableName(extractTableName(sql))
                    .executed(false)
                    .rowCount(null)
                    .errorMessage(safe ? null : safety.getReason())
                    .createdAt(LocalDateTime.now())
                    .build();
            try {
                queryHistoryService.saveContext(ctx);
            } catch (Exception e) {
                // Never let context persistence failure break the user-facing call.
                log.warn("Failed to persist conversation context: {}", e.getMessage());
            }
        }

        return NlQueryResponse.builder()
                .id(history.getId())
                .sessionId(effectiveSessionId)
                .originalQuery(request.getQueryText())
                .resolvedQuery(resolved)
                .generatedSql(sql)
                .intent(intent)
                .queryStatus(status)
                .errorMessage(safe ? null : safety.getReason())
                .sqlSafe(safe)
                .sqlViolations(safety.getViolations())
                .visualization(visualization == null ? null : buildVisualizationData(visualization, intent))
                .timestamp(history.getCreatedAt())
                .build();
    }

    // ---------- SQL-only validation ----------

    /** Validate a SQL string without executing it or persisting anything. */
    public SqlSafetyValidator.ValidationResult validateSql(String sql) {
        return sqlSafetyValidator.validate(sql);
    }

    // ---------- private helpers ----------

    private String interpretIntent(String queryText) {
        String lowerQuery = queryText.toLowerCase();

        if (lowerQuery.contains("total") || lowerQuery.contains("sum") || lowerQuery.contains("count") || lowerQuery.contains("总数")) {
            return "AGGREGATION";
        }
        if (lowerQuery.contains("trend") || lowerQuery.contains("over") || lowerQuery.contains("change") || lowerQuery.contains("趋势")) {
            return "TREND_ANALYSIS";
        }
        if (lowerQuery.contains("compare") || lowerQuery.contains("difference") || lowerQuery.contains("versus") || lowerQuery.contains("对比")) {
            return "COMPARISON";
        }
        if (lowerQuery.contains("top") || lowerQuery.contains("highest") || lowerQuery.contains("lowest") || lowerQuery.contains("前 ")) {
            return "RANKING";
        }
        if (lowerQuery.contains("average") || lowerQuery.contains("mean") || lowerQuery.contains("平均")) {
            return "AVERAGE";
        }
        if (lowerQuery.contains("percentage") || lowerQuery.contains("percent") || lowerQuery.contains("百分比")) {
            return "PERCENTAGE";
        }

        return "GENERAL_QUERY";
    }

    /** Pull the first table identifier out of a generated FROM clause. */
    private String extractTableName(String sql) {
        if (sql == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\bFROM\\s+([A-Za-z_][A-Za-z0-9_]*)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(sql);
        return m.find() ? m.group(1) : null;
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

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config);
        } catch (Exception e) {
            return "{}";
        }
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
