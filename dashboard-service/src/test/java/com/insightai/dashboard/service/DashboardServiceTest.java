package com.insightai.dashboard.service;

import com.insightai.dashboard.dto.*;
import com.insightai.dashboard.entity.Dashboard;
import com.insightai.dashboard.entity.DashboardWidget;
import com.insightai.dashboard.entity.WidgetSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dashboard Service unit tests
 *
 * Tests the core business logic for managing dashboards, widgets, layouts, and snapshots.
 * Uses pure unit tests (no Spring context) to keep the suite fast and isolated.
 */
@DisplayName("DashboardService business logic")
class DashboardServiceTest {

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        // Use in-memory fake repositories for unit testing
        dashboardService = new DashboardService(
                new InMemoryDashboardRepository(),
                new InMemoryWidgetRepository(),
                new InMemorySnapshotRepository()
        );
    }

    // ==================== Dashboard CRUD ====================

    @Test
    @DisplayName("creates a dashboard with default settings when visibility is omitted")
    void createsDashboardWithDefaults() {
        CreateDashboardRequest request = CreateDashboardRequest.builder()
                .name("Sales Overview")
                .description("Top-level sales KPIs")
                .ownerId("user-1")
                .build();

        DashboardDto result = dashboardService.createDashboard(request);

        assertNotNull(result.getId(), "id should be assigned");
        assertEquals("Sales Overview", result.getName());
        assertEquals("user-1", result.getOwnerId());
        assertEquals("PRIVATE", result.getVisibility(), "default visibility is PRIVATE");
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getWidgetCount() == 0);
    }

    @Test
    @DisplayName("rejects a dashboard with blank name")
    void rejectsBlankName() {
        CreateDashboardRequest request = CreateDashboardRequest.builder()
                .name("   ")
                .ownerId("user-1")
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dashboardService.createDashboard(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("updates dashboard name and description but preserves owner")
    void updatesDashboardFields() {
        DashboardDto created = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("Original")
                .ownerId("user-1")
                .build());

        UpdateDashboardRequest update = UpdateDashboardRequest.builder()
                .name("Renamed")
                .description("New desc")
                .build();

        DashboardDto updated = dashboardService.updateDashboard(created.getId(), update);

        assertEquals("Renamed", updated.getName());
        assertEquals("New desc", updated.getDescription());
        assertEquals("user-1", updated.getOwnerId(), "owner should not be changeable via update");
    }

    @Test
    @DisplayName("throws when updating a non-existent dashboard")
    void updateThrowsForMissingDashboard() {
        UpdateDashboardRequest update = UpdateDashboardRequest.builder().name("X").build();

        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.updateDashboard(999L, update));
    }

    @Test
    @DisplayName("deletes a dashboard and cascades widgets and snapshots")
    void deleteCascades() {
        DashboardDto created = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("To delete")
                .ownerId("user-1")
                .build());
        Long id = created.getId();

        dashboardService.addWidget(id, AddWidgetRequest.builder()
                .title("W1").queryId("q1").chartType("LINE").build());
        dashboardService.captureSnapshot(id, "manual");

        dashboardService.deleteDashboard(id);

        assertFalse(dashboardService.getDashboard(id).isPresent());
        assertTrue(dashboardService.listWidgets(id).isEmpty());
        assertTrue(dashboardService.listSnapshots(id).isEmpty());
    }

    // ==================== Listing & Filtering ====================

    @Test
    @DisplayName("lists dashboards by owner and visibility")
    void listByOwnerAndVisibility() {
        dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("Mine private").ownerId("u1").build());
        dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("Mine team").ownerId("u1").visibility("TEAM").build());
        dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("Other private").ownerId("u2").build());

        List<DashboardDto> u1All = dashboardService.listDashboards("u1", null);
        assertEquals(2, u1All.size());

        List<DashboardDto> u1Team = dashboardService.listDashboards("u1", "TEAM");
        assertEquals(1, u1Team.size());
        assertEquals("TEAM", u1Team.get(0).getVisibility());
    }

    @Test
    @DisplayName("changes visibility transitions are validated")
    void visibilityTransitions() {
        DashboardDto created = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());

        // valid
        DashboardDto team = dashboardService.changeVisibility(created.getId(), "TEAM");
        assertEquals("TEAM", team.getVisibility());

        // invalid value
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.changeVisibility(created.getId(), "GOD_MODE"));
    }

    // ==================== Widget Management ====================

    @Test
    @DisplayName("adds a widget with grid position and increments widget count")
    void addWidget() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());

        AddWidgetRequest req = AddWidgetRequest.builder()
                .title("Revenue trend")
                .queryId("q-revenue")
                .chartType("LINE")
                .positionX(0).positionY(0)
                .width(6).height(4)
                .build();

        DashboardWidgetDto w = dashboardService.addWidget(dash.getId(), req);

        assertNotNull(w.getId());
        assertEquals(0, w.getPositionX());
        assertEquals(6, w.getWidth());
        assertEquals("LINE", w.getChartType());
        assertEquals(1, dashboardService.getDashboard(dash.getId()).get().getWidgetCount());
    }

    @Test
    @DisplayName("moving a widget within the same dashboard updates its position")
    void moveWidget() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());
        DashboardWidgetDto w = dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("W").queryId("q").chartType("BAR")
                .positionX(0).positionY(0).width(4).height(3).build());

        DashboardWidgetDto moved = dashboardService.moveWidget(dash.getId(), w.getId(), 6, 3);

        assertEquals(6, moved.getPositionX());
        assertEquals(3, moved.getPositionY());
    }

    @Test
    @DisplayName("rejects overlapping widget placement on add")
    void rejectsOverlapOnAdd() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());
        dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("A").queryId("q1").chartType("LINE")
                .positionX(0).positionY(0).width(4).height(3).build());

        AddWidgetRequest overlap = AddWidgetRequest.builder()
                .title("B").queryId("q2").chartType("BAR")
                .positionX(2).positionY(1).width(4).height(3).build();

        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.addWidget(dash.getId(), overlap));
    }

    @Test
    @DisplayName("removing a widget shifts later widgets up (compacts the grid)")
    void removeCompactsGrid() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());
        DashboardWidgetDto w1 = dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("w1").queryId("q1").chartType("LINE")
                .positionX(0).positionY(0).width(6).height(2).build());
        DashboardWidgetDto w2 = dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("w2").queryId("q2").chartType("BAR")
                .positionX(0).positionY(2).width(6).height(2).build());

        dashboardService.removeWidget(dash.getId(), w1.getId());

        List<DashboardWidgetDto> remaining = dashboardService.listWidgets(dash.getId());
        assertEquals(1, remaining.size());
        // w2 should shift up to y=0
        assertEquals(0, remaining.get(0).getPositionY());
    }

    // ==================== Snapshots ====================

    @Test
    @DisplayName("captures a snapshot with the rendered chart payload")
    void captureSnapshot() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());
        dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("Revenue").queryId("q-rev").chartType("LINE")
                .positionX(0).positionY(0).width(6).height(4).build());

        Map<String, Object> payload = Map.of(
                "data", List.of(Map.of("x", "2026-01", "y", 1234)),
                "summary", Map.of("total", 1234, "currency", "USD")
        );

        WidgetSnapshotDto snap = dashboardService.captureWidgetSnapshot(
                dash.getId(), 1L, payload, "scheduled");

        assertNotNull(snap.getId());
        assertEquals(BigDecimal.valueOf(1234), snap.getMetricValue());
        assertEquals("scheduled", snap.getTriggeredBy());
    }

    @Test
    @DisplayName("limits the number of retained snapshots per widget (retention policy)")
    void retentionLimit() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());
        dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("W").queryId("q").chartType("LINE")
                .positionX(0).positionY(0).width(4).height(3).build());

        for (int i = 0; i < 5; i++) {
            dashboardService.captureWidgetSnapshot(
                    dash.getId(), 1L,
                    Map.of("data", List.of(Map.of("x", "t" + i, "y", i))),
                    "auto");
        }

        List<WidgetSnapshotDto> snaps = dashboardService.listSnapshotsForWidget(dash.getId(), 1L);
        // Default retention is 3
        assertEquals(3, snaps.size());
    }

    // ==================== Layout / Statistics ====================

    @Test
    @DisplayName("computes dashboard statistics: widget count, area, last update")
    void dashboardStatistics() {
        DashboardDto dash = dashboardService.createDashboard(CreateDashboardRequest.builder()
                .name("D").ownerId("u1").build());
        dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("w1").queryId("q1").chartType("LINE")
                .positionX(0).positionY(0).width(4).height(3).build());
        dashboardService.addWidget(dash.getId(), AddWidgetRequest.builder()
                .title("w2").queryId("q2").chartType("BAR")
                .positionX(4).positionY(0).width(4).height(3).build());

        DashboardStatisticsDto stats = dashboardService.getStatistics(dash.getId());

        assertEquals(2, stats.getWidgetCount());
        assertEquals(24, stats.getTotalArea()); // 2 * (4*3)
        assertNotNull(stats.getLastUpdatedAt());
    }

    // ==================== In-memory test doubles ====================

    static class InMemoryDashboardRepository extends InMemoryCrudRepository<Dashboard> {
        public InMemoryDashboardRepository() { super(Dashboard::getId, Dashboard::setId); }
    }
    static class InMemoryWidgetRepository extends InMemoryCrudRepository<DashboardWidget> {
        public InMemoryWidgetRepository() { super(DashboardWidget::getId, DashboardWidget::setId); }
    }
    static class InMemorySnapshotRepository extends InMemoryCrudRepository<WidgetSnapshot> {
        public InMemorySnapshotRepository() { super(WidgetSnapshot::getId, WidgetSnapshot::setId); }
    }
}
