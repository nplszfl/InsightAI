package com.insightai.ai.controller;

import com.insightai.ai.dto.*;
import com.insightai.ai.service.AttributionAnalysisService;
import com.insightai.ai.service.AutoReportGenerationService;
import com.insightai.ai.service.NaturalLanguageToSqlService;
import com.insightai.ai.service.TimeSeriesForecastingService;
import com.insightai.ai.service.VisualizationRecommendationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final NaturalLanguageToSqlService naturalLanguageToSqlService;
    private final VisualizationRecommendationService visualizationRecommendationService;
    private final TimeSeriesForecastingService timeSeriesForecastingService;
    private final AutoReportGenerationService autoReportGenerationService;
    private final AttributionAnalysisService attributionAnalysisService;

    public AiController(
            NaturalLanguageToSqlService naturalLanguageToSqlService,
            VisualizationRecommendationService visualizationRecommendationService,
            TimeSeriesForecastingService timeSeriesForecastingService,
            AutoReportGenerationService autoReportGenerationService,
            AttributionAnalysisService attributionAnalysisService) {
        this.naturalLanguageToSqlService = naturalLanguageToSqlService;
        this.visualizationRecommendationService = visualizationRecommendationService;
        this.timeSeriesForecastingService = timeSeriesForecastingService;
        this.autoReportGenerationService = autoReportGenerationService;
        this.attributionAnalysisService = attributionAnalysisService;
    }

    @PostMapping("/query/convert")
    public ResponseEntity<SqlConversionResponse> convertQuery(
            @Valid @RequestBody NaturalLanguageQueryRequest request) {
        log.info("Received query conversion request: {}", request.getQuery());
        SqlConversionResponse response = naturalLanguageToSqlService.convertToSql(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/visualization/recommend")
    public ResponseEntity<VisualizationRecommendationResponse> recommendVisualization(
            @Valid @RequestBody VisualizationRecommendationRequest request) {
        log.info("Received visualization recommendation request for {} columns", request.getColumns().size());
        VisualizationRecommendationResponse response = visualizationRecommendationService.recommend(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forecast")
    public ResponseEntity<TimeSeriesForecastResponse> forecast(
            @Valid @RequestBody TimeSeriesForecastRequest request) {
        log.info("Received forecast request for {} data points over {} periods",
                request.getDataPoints().size(), request.getForecastPeriods());
        TimeSeriesForecastResponse response = timeSeriesForecastingService.forecast(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/report/generate")
    public ResponseEntity<ReportGenerationResponse> generateReport(
            @Valid @RequestBody ReportGenerationRequest request) {
        log.info("Received report generation request for topic: {}", request.getTopic());
        ReportGenerationResponse response = autoReportGenerationService.generateReport(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attribution/analyze")
    public ResponseEntity<AttributionAnalysisResponse> analyzeAttribution(
            @Valid @RequestBody AttributionAnalysisRequest request) {
        log.info("Received attribution analysis request for metric: {}", request.getMetricName());
        AttributionAnalysisResponse response = attributionAnalysisService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.debug("Health check requested");
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .service("insightai-ai-service")
                .version("1.0.0")
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.ok(response);
    }
}