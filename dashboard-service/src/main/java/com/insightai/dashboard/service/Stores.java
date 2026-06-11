package com.insightai.dashboard.service;

import com.insightai.dashboard.entity.Dashboard;
import com.insightai.dashboard.entity.DashboardWidget;
import com.insightai.dashboard.entity.WidgetSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Internal store interfaces for the dashboard service. Keeps the service
 * persistence-agnostic — production uses MyBatis-Plus mapper-backed
 * implementations, tests use simple in-memory implementations.
 */
public interface DashboardStore {
    Dashboard save(Dashboard dashboard);
    Optional<Dashboard> findById(Long id);
    List<Dashboard> findAll();
    List<Dashboard> findBy(Predicate<Dashboard> predicate);
    void deleteById(Long id);
}

interface WidgetStore {
    DashboardWidget save(DashboardWidget widget);
    Optional<DashboardWidget> findById(Long id);
    List<DashboardWidget> findBy(Predicate<DashboardWidget> predicate);
    int countByDashboard(Long dashboardId);
    void deleteById(Long id);
    void deleteByDashboard(Long dashboardId);
}

interface SnapshotStore {
    WidgetSnapshot save(WidgetSnapshot snapshot);
    List<WidgetSnapshot> findBy(Predicate<WidgetSnapshot> predicate);
    void deleteById(Long id);
    void deleteByDashboard(Long dashboardId);
}
