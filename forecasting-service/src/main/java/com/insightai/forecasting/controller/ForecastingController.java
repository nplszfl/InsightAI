package com.insightai.forecasting.controller;

import com.insightai.common.dto.ApiResponse;
import com.insightai.forecasting.dto.*;
import com.insightai.forecasting.service.ForecastingService;
import com.insightai.forecasting.service.TrendAnalysisService;
import com.insightai.forecasting.service.SeasonalityDetectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forecasting")
@RequiredArgsConstructor
public class ForecastingController {
    
    private final ForecastingService forecastingService;
    private final TrendAnalysisService trendAnalysisService;
    private final SeasonalityDetectionService seasonalityDetectionService;
    
    @PostMapping("/forecast")
    public ApiResponse<List<ForecastResponse>> forecast(@Valid @RequestBody ForecastRequest request) {
        List<ForecastResponse> result = forecastingService.forecast(request);
        return ApiResponse.success("Forecast generated successfully", result);
    }
    
    @PostMapping("/trend")
    public ApiResponse<TrendAnalysisResponse> analyzeTrend(@Valid @RequestBody TrendAnalysisRequest request) {
        TrendAnalysisResponse result = trendAnalysisService.analyze(request);
        return ApiResponse.success("Trend analysis completed", result);
    }
    
    @PostMapping("/seasonality")
    public ApiResponse<SeasonalityResponse> detectSeasonality(@Valid @RequestBody SeasonalityRequest request) {
        SeasonalityResponse result = seasonalityDetectionService.detect(request);
        return ApiResponse.success("Seasonality detection completed", result);
    }
    
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Forecasting service is healthy", "OK");
    }
}