package com.insightai.anomaly.controller;

import com.insightai.common.dto.ApiResponse;
import com.insightai.anomaly.dto.*;
import com.insightai.anomaly.entity.AnomalyRecord;
import com.insightai.anomaly.service.AnomalyDetectionService;
import com.insightai.anomaly.service.AlertService;
import com.insightai.anomaly.service.AnomalyHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/anomaly")
@RequiredArgsConstructor
public class AnomalyController {
    
    private final AnomalyDetectionService anomalyDetectionService;
    private final AlertService alertService;
    private final AnomalyHistoryService anomalyHistoryService;
    
    @PostMapping("/detect")
    public ApiResponse<List<AnomalyResponse>> detect(@Valid @RequestBody AnomalyDetectionRequest request) {
        List<AnomalyResponse> result = anomalyDetectionService.detect(request);
        return ApiResponse.success("Anomaly detection completed", result);
    }
    
    @PostMapping("/alert")
    public ApiResponse<AlertResponse> sendAlert(@Valid @RequestBody AlertRequest request) {
        AlertResponse result = alertService.sendAlert(request);
        return ApiResponse.success("Alert sent successfully", result);
    }
    
    @GetMapping("/history")
    public ApiResponse<List<AnomalyRecord>> getHistory(
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<AnomalyRecord> result = anomalyHistoryService.getHistory(metricName, startTime, endTime);
        return ApiResponse.success("History retrieved successfully", result);
    }
    
    @PutMapping("/acknowledge/{id}")
    public ApiResponse<Boolean> acknowledgeAnomaly(
            @PathVariable Long id,
            @RequestParam String acknowledgedBy) {
        boolean result = anomalyHistoryService.acknowledgeAnomaly(id, acknowledgedBy);
        return ApiResponse.success("Anomaly acknowledged", result);
    }
    
    @GetMapping("/count/unacknowledged")
    public ApiResponse<Long> getUnacknowledgedCount(@RequestParam(required = false) String metricName) {
        long count = anomalyHistoryService.getUnacknowledgedCount(metricName);
        return ApiResponse.success("Unacknowledged count", count);
    }
    
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Anomaly detection service is healthy", "OK");
    }
}