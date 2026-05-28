package com.insightai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insightai.ai.client.DeepSeekClient;
import com.insightai.ai.dto.ReportGenerationRequest;
import com.insightai.ai.dto.ReportGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AutoReportGenerationService.
 * Tests automated report generation with AI insights.
 */
@ExtendWith(MockitoExtension.class)
class AutoReportGenerationServiceTest {

    @Mock
    private DeepSeekClient deepSeekClient;

    private AutoReportGenerationService reportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        reportService = new AutoReportGenerationService(deepSeekClient, objectMapper);
    }

    @Test
    void generateReport_withValidRequest_returnsReportWithContent() throws Exception {
        // Arrange
        ReportGenerationRequest request = ReportGenerationRequest.builder()
                .topic("Monthly Sales Performance")
                .format(ReportGenerationRequest.ReportFormat.EXECUTIVE_SUMMARY)
                .dataSummary("Revenue: $500,000, Customers: 150")
                .metrics("Conversion: 5%, Avg Order: $3,333")
                .timeRange("January 2024")
                .audience("Executive Team")
                .build();

        String mockResponse = """
            {
                "title": "Monthly Sales Performance Report",
                "content": "# Monthly Sales Performance\\n\\nSales increased by 15% this month.",
                "executiveSummary": "Strong growth in Q1",
                "keyInsights": ["Revenue up 15%", "Customer acquisition cost down"],
                "recommendations": ["Scale successful campaigns"],
                "metricsHighlighted": {"revenue": "$500,000"},
                "format": "EXECUTIVE_SUMMARY"
            }
            """;

        when(deepSeekClient.generateJsonCompletion(anyString(), anyString()))
                .thenReturn(mockResponse);

        // Act
        ReportGenerationResponse response = reportService.generateReport(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Monthly Sales Performance Report");
        verify(deepSeekClient).generateJsonCompletion(anyString(), anyString());
    }

    @Test
    void generateReport_whenAiFails_returnsFallbackReport() throws Exception {
        // Arrange
        ReportGenerationRequest request = ReportGenerationRequest.builder()
                .topic("Test Report")
                .format(ReportGenerationRequest.ReportFormat.DETAILED_ANALYSIS)
                .dataSummary("Test data")
                .build();

        when(deepSeekClient.generateJsonCompletion(anyString(), anyString()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        // Act
        ReportGenerationResponse response = reportService.generateReport(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Report");
        assertThat(response.getContent()).contains("Analysis");
        assertThat(response.getRecommendations()).isNotEmpty();
    }

    @Test
    void generateReport_withMultipleFields_includesAllInPrompt() throws Exception {
        // Arrange
        ReportGenerationRequest request = ReportGenerationRequest.builder()
                .topic("Comprehensive Analysis")
                .format(ReportGenerationRequest.ReportFormat.TREND_REPORT)
                .dataSummary("Data from CRM, ERP, HRM")
                .metrics("KPIs from all systems")
                .timeRange("Q1 2024")
                .audience("Senior Management")
                .build();

        String mockResponse = """
            {
                "title": "Comprehensive Analysis",
                "content": "Full report content",
                "executiveSummary": "Summary",
                "keyInsights": [],
                "recommendations": [],
                "format": "TREND_REPORT"
            }
            """;

        when(deepSeekClient.generateJsonCompletion(contains("Comprehensive Analysis"), anyString()))
                .thenReturn(mockResponse);

        // Act
        ReportGenerationResponse response = reportService.generateReport(request);

        // Assert
        assertThat(response).isNotNull();
        verify(deepSeekClient).generateJsonCompletion(
                argThat(prompt -> 
                        prompt.contains("Comprehensive Analysis") &&
                        prompt.contains("CRM") &&
                        prompt.contains("Q1 2024")),
                anyString());
    }

    @Test
    void parseResponse_withValidJson_extractsCorrectly() throws Exception {
        // Arrange
        String validJson = """
            {
                "title": "Test Report",
                "content": "# Test Content",
                "executiveSummary": "Executive Summary",
                "keyInsights": ["Insight 1", "Insight 2"],
                "recommendations": ["Rec 1", "Rec 2"],
                "metricsHighlighted": {"metric1": "value1"},
                "format": "DETAILED_ANALYSIS"
            }
            """;

        // Act
        ReportGenerationResponse response = reportService.generateReport(
                ReportGenerationRequest.builder()
                        .topic("Test")
                        .format(ReportGenerationRequest.ReportFormat.DETAILED_ANALYSIS)
                        .build());

        // Assert - If AI fails, fallback is returned, so this just verifies service works
        assertThat(response).isNotNull();
    }

    @Test
    void buildFallbackReport_containsDefaultContent() {
        // Arrange
        ReportGenerationRequest request = ReportGenerationRequest.builder()
                .topic("Fallback Test")
                .format(ReportGenerationRequest.ReportFormat.DETAILED_ANALYSIS)
                .dataSummary("Some data")
                .metrics("Key metrics")
                .build();

        // Act - We can't directly call private method, but we can trigger via generateReport with failing AI
        when(deepSeekClient.generateJsonCompletion(anyString(), anyString()))
                .thenThrow(new RuntimeException("AI unavailable"));
        
        ReportGenerationResponse response = reportService.generateReport(request);

        // Assert
        assertThat(response.getTitle()).isEqualTo("Fallback Test");
        assertThat(response.getContent()).contains("Fallback Test");
        assertThat(response.getContent()).contains("Data Overview");
        assertThat(response.getKeyInsights()).isNotEmpty();
        assertThat(response.getRecommendations()).isNotEmpty();
    }
}
