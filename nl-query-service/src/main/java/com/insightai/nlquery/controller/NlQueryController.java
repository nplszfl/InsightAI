package com.insightai.nlquery.controller;

import com.insightai.common.dto.ApiResponse;
import com.insightai.nlquery.dto.ConversationContextDto;
import com.insightai.nlquery.dto.NlQueryRequest;
import com.insightai.nlquery.dto.NlQueryResponse;
import com.insightai.nlquery.service.NlQueryService;
import com.insightai.nlquery.service.QueryHistoryService;
import com.insightai.nlquery.service.SqlSafetyValidator;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nl")
@RequiredArgsConstructor
public class NlQueryController {

    private final NlQueryService nlQueryService;
    private final QueryHistoryService queryHistoryService;

    /** POST /api/nl/query — process a NL query, optionally carrying a sessionId for multi-turn. */
    @PostMapping("/query")
    public ApiResponse<NlQueryResponse> processQuery(@Valid @RequestBody NlQueryRequest request) {
        NlQueryResponse result = nlQueryService.processQueryWithSession(
                request, request.getSessionId(), 5);
        return ApiResponse.success("Query processed successfully", result);
    }

    /** GET /api/nl/session/{sessionId}/history — recent turns for a session. */
    @GetMapping("/session/{sessionId}/history")
    public ApiResponse<List<ConversationContextDto>> history(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<ConversationContextDto> history = queryHistoryService.getRecentBySession(sessionId, safeLimit);
        return ApiResponse.success("History fetched", history);
    }

    /** POST /api/nl/validate — validate SQL only, no execution, no persistence. */
    @PostMapping("/validate")
    public ApiResponse<Map<String, Object>> validate(@RequestBody ValidateRequest body) {
        SqlSafetyValidator.ValidationResult result = nlQueryService.validateSql(body == null ? null : body.getSql());
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("valid", result.isValid());
        payload.put("violations", result.getViolations());
        payload.put("reason", result.getReason());
        return ApiResponse.success(
                result.isValid() ? "SQL is safe" : "SQL rejected",
                payload);
    }

    /** GET /api/nl/health — liveness probe. */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("NL Query service is healthy", "OK");
    }

    @Data
    public static class ValidateRequest {
        private String sql;
    }
}
