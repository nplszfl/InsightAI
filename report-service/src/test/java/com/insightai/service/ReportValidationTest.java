package com.insightai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insightai.common.dto.ReportDto;
import com.insightai.common.model.Report;
import com.insightai.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the report validation business rules.
 *
 * <p>RED first: the test was written before any production changes were needed.
 */
@DisplayName("ReportService.validateReport")
class ReportValidationTest {

    private ReportService service;

    @BeforeEach
    void setUp() {
        ReportRepository repo = mock(ReportRepository.class);
        // No report with a duplicate name exists by default
        when(repo.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        service = new ReportService();
        ReflectionTestUtils.setField(service, "baseMapper", repo);
    }

    @Test
    @DisplayName("rejects null DTO")
    void rejectsNullDto() {
        assertThatThrownBy(() -> service.validateReport(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("rejects blank report names")
    void rejectsBlankName() {
        ReportDto dto = ReportDto.builder().name("   ").type("DASHBOARD").build();
        assertThatThrownBy(() -> service.validateReport(dto, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    @DisplayName("rejects unsupported report types")
    void rejectsBadType() {
        ReportDto dto = ReportDto.builder().name("Q1").type("UNKNOWN").build();
        assertThatThrownBy(() -> service.validateReport(dto, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid report type");
    }

    @Test
    @DisplayName("accepts a well-formed DASHBOARD report")
    void acceptsGoodDashboard() {
        service.validateReport(ReportDto.builder().name("Q1 Sales").type("DASHBOARD").build(), null);
    }

    @Test
    @DisplayName("accepts SCHEDULED and AD_HOC types")
    void acceptsOtherValidTypes() {
        service.validateReport(ReportDto.builder().name("A").type("SCHEDULED").build(), null);
        service.validateReport(ReportDto.builder().name("B").type("AD_HOC").build(), null);
    }

    @Test
    @DisplayName("isValidReportType covers the public contract")
    void isValidReportTypeContract() {
        assertThat(service.isValidReportType("DASHBOARD")).isTrue();
        assertThat(service.isValidReportType("SCHEDULED")).isTrue();
        assertThat(service.isValidReportType("AD_HOC")).isTrue();
        assertThat(service.isValidReportType(null)).isFalse();
        assertThat(service.isValidReportType("")).isFalse();
        assertThat(service.isValidReportType("random")).isFalse();
    }
}
