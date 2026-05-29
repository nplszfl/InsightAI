package com.insightai.nlquery.controller;

import com.insightai.common.dto.ApiResponse;
import com.insightai.nlquery.dto.NlQueryRequest;
import com.insightai.nlquery.dto.NlQueryResponse;
import com.insightai.nlquery.service.NlQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nl-query")
@RequiredArgsConstructor
public class NlQueryController {
    
    private final NlQueryService nlQueryService;
    
    @PostMapping("/query")
    public ApiResponse<NlQueryResponse> processQuery(@Valid @RequestBody NlQueryRequest request) {
        NlQueryResponse result = nlQueryService.processQuery(request);
        return ApiResponse.success("Query processed successfully", result);
    }
    
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("NL Query service is healthy", "OK");
    }
}