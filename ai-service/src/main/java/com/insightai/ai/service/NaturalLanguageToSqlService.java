package com.insightai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.ai.client.DeepSeekClient;
import com.insightai.ai.dto.NaturalLanguageQueryRequest;
import com.insightai.ai.dto.SqlConversionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NaturalLanguageToSqlService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert SQL query generator. Your task is to convert natural language queries into SQL queries.
            
            Guidelines:
            1. Output ONLY a valid JSON object with the following structure:
               {
                 "sql": "SELECT ..." ,
                 "explanation": "Brief explanation of what the SQL does",
                 "confidence": 0.95
               }
            2. The confidence score should be between 0 and 1.
            3. If the query is ambiguous, make reasonable assumptions and explain them.
            4. Use standard SQL syntax that is compatible with most database systems.
            5. Include appropriate JOINs, WHERE clauses, GROUP BY, and ORDER BY as needed.
            6. Never include DROP, DELETE, or UPDATE statements - only SELECT queries.
            """;

    public NaturalLanguageToSqlService(DeepSeekClient deepSeekClient, ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    public SqlConversionResponse convertToSql(NaturalLanguageQueryRequest request) {
        log.info("Converting natural language query: {}", request.getQuery());

        String prompt = buildPrompt(request);

        try {
            String jsonResponse = deepSeekClient.generateJsonCompletion(prompt, SYSTEM_PROMPT);
            return parseResponse(jsonResponse);
        } catch (Exception e) {
            log.error("Failed to convert query to SQL: {}", e.getMessage());
            return buildFallbackResponse(request.getQuery());
        }
    }

    private String buildPrompt(NaturalLanguageQueryRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Convert the following natural language query to SQL:\n\n");
        prompt.append("Query: ").append(request.getQuery()).append("\n\n");

        if (request.getDatabaseSchema() != null && !request.getDatabaseSchema().isEmpty()) {
            prompt.append("Database Schema:\n").append(request.getDatabaseSchema()).append("\n\n");
        }

        if (request.getDatabaseType() != null && !request.getDatabaseType().isEmpty()) {
            prompt.append("Database Type: ").append(request.getDatabaseType()).append("\n");
        }

        return prompt.toString();
    }

    private SqlConversionResponse parseResponse(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, SqlConversionResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, attempting to extract SQL manually");
            return extractSqlManually(jsonResponse);
        }
    }

    private SqlConversionResponse extractSqlManually(String jsonResponse) {
        try {
            String sql = extractJsonValue(jsonResponse, "sql");
            String explanation = extractJsonValue(jsonResponse, "explanation");
            double confidence = 0.7;

            return SqlConversionResponse.builder()
                    .sql(sql != null ? sql : "SELECT * FROM table -- Unable to parse")
                    .explanation(explanation != null ? explanation : "Parse error - please review")
                    .confidence(confidence)
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract SQL from response", e);
            return SqlConversionResponse.builder()
                    .sql("-- Error generating SQL")
                    .explanation("An error occurred during SQL generation")
                    .confidence(0.0)
                    .build();
        }
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private SqlConversionResponse buildFallbackResponse(String query) {
        return SqlConversionResponse.builder()
                .sql("-- Could not generate SQL for: " + query)
                .explanation("The AI service was unable to generate SQL for this query. Please try rephrasing or provide more context.")
                .confidence(0.0)
                .build();
    }
}