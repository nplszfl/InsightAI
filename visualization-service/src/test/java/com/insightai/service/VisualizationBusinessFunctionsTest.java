package com.insightai.service;

import com.insightai.common.model.Visualization;
import com.insightai.repository.VisualizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Behaviour-driven tests for the visualization validation and bulk business
 * functions added to {@link VisualizationService}.
 *
 * <p>RED: the test exercises methods that are not yet on the service
 * ({@code validateVisualization}, {@code validateConfig}, {@code exportToCsv},
 * {@code cloneVisualizationsToReport}, {@code getDashboardLayout}). Until
 * the production code is added the file will not even compile, which is the
 * whole point of the RED step.
 */
@DisplayName("VisualizationService business functions")
class VisualizationBusinessFunctionsTest {

    private VisualizationRepository repository;
    private VisualizationService service;

    @BeforeEach
    void setUp() {
        repository = mock(VisualizationRepository.class);
        // VisualisationService uses ServiceImpl's base save/getById etc., so we
        // build a real instance with a mocked repository.
        service = new VisualizationService();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "baseMapper", repository);
    }

    @Test
    @DisplayName("validates chart type against the supported catalogue")
    void validatesChartType() {
        assertThat(service.isValidChartType("TABLE")).isTrue();
        assertThat(service.isValidChartType("BAR")).isTrue();
        assertThat(service.isValidChartType("LINE")).isTrue();
        assertThat(service.isValidChartType("PIE")).isTrue();
        assertThat(service.isValidChartType("UNKNOWN_CHART")).isFalse();
        assertThat(service.isValidChartType(null)).isFalse();
        assertThat(service.isValidChartType("")).isFalse();
    }

    @Test
    @DisplayName("rejects visualisations with invalid chart type")
    void validateVisualizationRejectsInvalidChartType() {
        Visualization bad = Visualization.builder()
                .chartType("INVALID")
                .title("Revenue")
                .build();
        assertThatThrownBy(() -> service.validateVisualization(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid chart type");
    }

    @Test
    @DisplayName("rejects visualisations with blank title")
    void validateVisualizationRejectsBlankTitle() {
        Visualization bad = Visualization.builder()
                .chartType("BAR")
                .title("   ")
                .build();
        assertThatThrownBy(() -> service.validateVisualization(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title is required");
    }

    @Test
    @DisplayName("rejects negative positions")
    void validateVisualizationRejectsNegativePosition() {
        Visualization bad = Visualization.builder()
                .chartType("BAR")
                .title("OK")
                .position(-1)
                .build();
        assertThatThrownBy(() -> service.validateVisualization(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Position");
    }

    @Test
    @DisplayName("accepts a well-formed visualisation")
    void validateVisualizationAcceptsGoodInput() {
        Visualization good = Visualization.builder()
                .chartType("BAR")
                .title("Q1 Revenue")
                .position(0)
                .build();
        service.validateVisualization(good); // should not throw
    }

    @Test
    @DisplayName("parses and validates a JSON config payload")
    void parsesAndValidatesJsonConfig() {
        // Valid: includes xField and yField
        var parsed = service.validateConfig("{\"xField\":\"month\",\"yField\":\"revenue\"}");
        assertThat(parsed.get("xField")).isEqualTo("month");
        assertThat(parsed.get("yField")).isEqualTo("revenue");

        // Missing yField
        assertThatThrownBy(() -> service.validateConfig("{\"xField\":\"month\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yField");

        // Malformed JSON
        assertThatThrownBy(() -> service.validateConfig("{not json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not valid JSON");
    }

    @Test
    @DisplayName("exports a single chart's data to CSV")
    void exportsVisualizationToCsv() {
        // rows + columns
        String csv = service.exportToCsv(
                List.of("month", "revenue"),
                List.of(
                        List.of("2025-01", 1000),
                        List.of("2025-02", 2000),
                        List.of("2025-03", 1500)
                )
        );
        assertThat(csv).isEqualTo("month,revenue\n2025-01,1000\n2025-02,2000\n2025-03,1500\n");
    }

    @Test
    @DisplayName("escapes commas and quotes in CSV output")
    void csvEscapesSpecialCharacters() {
        String csv = service.exportToCsv(
                List.of("name", "note"),
                List.of(List.of("Acme, Inc.", "Says \"hi\""))
        );
        // name column contains a comma → must be wrapped in quotes
        // note column contains a quote → must be escaped
        assertThat(csv).isEqualTo("name,note\n\"Acme, Inc.\",\"Says \"\"hi\"\"\"\n");
    }

    @Test
    @DisplayName("rejects CSV export with mismatched column count")
    void csvRejectsMismatchedRowLength() {
        assertThatThrownBy(() -> service.exportToCsv(
                List.of("a", "b"),
                List.of(List.of("only-one"))
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("builds a dashboard layout (visualizations ordered by position)")
    void buildsDashboardLayout() {
        Visualization v1 = Visualization.builder().id(1L).chartType("BAR").title("A").position(0).reportId(99L).build();
        Visualization v2 = Visualization.builder().id(2L).chartType("LINE").title("B").position(1).reportId(99L).build();
        Visualization v3 = Visualization.builder().id(3L).chartType("PIE").title("C").position(2).reportId(99L).build();
        when(repository.selectList(any())).thenReturn(Arrays.asList(v3, v1, v2));

        var layout = service.getDashboardLayout(99L);

        assertThat(layout).isNotNull();
        assertThat(layout.getReportId()).isEqualTo(99L);
        assertThat(layout.getTotalCount()).isEqualTo(3);
        assertThat(layout.getChartTypeCounts())
                .containsEntry("BAR", 1L)
                .containsEntry("LINE", 1L)
                .containsEntry("PIE", 1L);
        // Must be in position order regardless of storage order
        assertThat(layout.getItems()).extracting("id").containsExactly(1L, 2L, 3L);
        assertThat(layout.getItems().get(0).getTitle()).isEqualTo("A");
    }
}
