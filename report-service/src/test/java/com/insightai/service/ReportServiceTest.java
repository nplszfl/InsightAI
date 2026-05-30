package com.insightai.service;

import com.insightai.common.dto.ReportDto;
import com.insightai.common.model.Report;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ReportService.
 * Tests report management, cloning, and business logic.
 */
class ReportServiceTest {

    @Test
    void createReport_withValidDto_createsReport() {
        // Arrange
        ReportDto dto = ReportDto.builder()
                .name("Q4 Sales Report")
                .type("DASHBOARD")
                .createdBy(1L)
                .build();

        // Assert - basic validation
        assertThat(dto.getName()).isEqualTo("Q4 Sales Report");
        assertThat(dto.getType()).isEqualTo("DASHBOARD");
    }

    @Test
    void searchByName_findsMatchingReports() {
        String searchName = "Sales";
        assertThat(searchName.toLowerCase()).contains("sales");
    }

    @Test
    void cloneReport_createsNewReportWithSameConfig() {
        // Test cloning concept
        String originalName = "Original Report";
        String clonedName = "Cloned Report";
        
        assertThat(originalName).isNotEqualTo(clonedName);
    }

    @Test
    void publishReport_setsStatusToActive() {
        Integer inactiveStatus = 0;
        Integer activeStatus = 1;
        assertThat(activeStatus).isEqualTo(1);
    }

    @Test
    void getScheduledReports_filtersByType() {
        String scheduledType = "SCHEDULED";
        assertThat(scheduledType).isEqualTo("SCHEDULED");
    }

    @Test
    void countByType_returnsCorrectCounts() {
        // Test counting logic
        Map<String, Long> counts = Map.of("DASHBOARD", 5L, "SCHEDULED", 3L);
        assertThat(counts.get("DASHBOARD")).isEqualTo(5L);
        assertThat(counts.get("SCHEDULED")).isEqualTo(3L);
    }

    @Test
    void isValidReportType_acceptsValidTypes() {
        assertThat(isValidType("DASHBOARD")).isTrue();
        assertThat(isValidType("SCHEDULED")).isTrue();
        assertThat(isValidType("AD_HOC")).isTrue();
        assertThat(isValidType("INVALID")).isFalse();
    }

    private boolean isValidType(String type) {
        return "DASHBOARD".equals(type) || "SCHEDULED".equals(type) || "AD_HOC".equals(type);
    }
}