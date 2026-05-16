package com.insightai.controller;

import com.insightai.common.dto.QueryRequest;
import com.insightai.common.dto.QueryResponse;
import com.insightai.common.model.QueryHistory;
import com.insightai.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queries")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @PostMapping("/execute")
    public ResponseEntity<QueryResponse> execute(@RequestBody QueryRequest request) {
        return ResponseEntity.ok(queryService.executeQuery(request));
    }

    @GetMapping("/{queryId}")
    public ResponseEntity<QueryResponse> getResult(@PathVariable String queryId) {
        return queryService.getQueryResult(queryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/{dataSourceId}")
    public ResponseEntity<List<QueryHistory>> getHistory(
            @PathVariable Long dataSourceId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(queryService.getQueryHistory(dataSourceId, limit));
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearCache() {
        queryService.clearCache();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cache/query")
    public ResponseEntity<Void> clearCacheForQuery(@RequestParam String query) {
        queryService.clearCacheForQuery(query);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(queryService.getCacheStats());
    }
}
