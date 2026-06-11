package com.insightai.dashboard.service;

import com.insightai.dashboard.entity.Dashboard;
import com.insightai.dashboard.entity.DashboardWidget;
import com.insightai.dashboard.entity.WidgetSnapshot;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * In-memory implementations of the store interfaces, used in unit tests
 * (and as a fallback when no database is configured).
 */
public final class InMemoryStores {

    private InMemoryStores() {}

    public static class DashboardStoreImpl implements DashboardStore {
        private final Map<Long, Dashboard> map = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(0);

        public Dashboard save(Dashboard d) {
            if (d.getId() == null) d.setId(seq.incrementAndGet());
            map.put(d.getId(), d);
            return d;
        }
        public Optional<Dashboard> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        public List<Dashboard> findAll() { return new ArrayList<>(map.values()); }
        public List<Dashboard> findBy(Predicate<Dashboard> p) {
            return map.values().stream().filter(p).collect(Collectors.toList());
        }
        public void deleteById(Long id) { map.remove(id); }
    }

    public static class WidgetStoreImpl implements WidgetStore {
        private final Map<Long, DashboardWidget> map = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(0);

        public DashboardWidget save(DashboardWidget w) {
            if (w.getId() == null) w.setId(seq.incrementAndGet());
            map.put(w.getId(), w);
            return w;
        }
        public Optional<DashboardWidget> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        public List<DashboardWidget> findBy(Predicate<DashboardWidget> p) {
            return map.values().stream().filter(p).collect(Collectors.toList());
        }
        public int countByDashboard(Long dashboardId) {
            return (int) map.values().stream()
                    .filter(w -> w.getDashboardId().equals(dashboardId))
                    .count();
        }
        public void deleteById(Long id) { map.remove(id); }
        public void deleteByDashboard(Long dashboardId) {
            map.values().removeIf(w -> w.getDashboardId().equals(dashboardId));
        }
    }

    public static class SnapshotStoreImpl implements SnapshotStore {
        private final Map<Long, WidgetSnapshot> map = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(0);

        public WidgetSnapshot save(WidgetSnapshot s) {
            if (s.getId() == null) s.setId(seq.incrementAndGet());
            map.put(s.getId(), s);
            return s;
        }
        public List<WidgetSnapshot> findBy(Predicate<WidgetSnapshot> p) {
            return map.values().stream().filter(p).collect(Collectors.toList());
        }
        public void deleteById(Long id) { map.remove(id); }
        public void deleteByDashboard(Long dashboardId) {
            map.values().removeIf(s -> s.getDashboardId().equals(dashboardId));
        }
    }
}
