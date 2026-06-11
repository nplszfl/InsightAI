package com.insightai.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.insightai.dashboard.entity.Dashboard;
import com.insightai.dashboard.entity.DashboardWidget;
import com.insightai.dashboard.entity.WidgetSnapshot;
import com.insightai.dashboard.mapper.DashboardMapper;
import com.insightai.dashboard.mapper.DashboardWidgetMapper;
import com.insightai.dashboard.mapper.WidgetSnapshotMapper;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * MyBatis-Plus backed store implementations. Used in production.
 */
public final class MybatisStores {

    private MybatisStores() {}

    /** Predicate-to-MyBatis-QueryWrapper adapter for the simple predicates
     *  used by DashboardService. The service predicates only inspect fields on
     *  the entity, so a simple in-memory filter after a full-table fetch is
     *  acceptable for the volumes we expect (per-user dashboards).
     */
    private static <T> List<T> filter(List<T> all, Predicate<T> p) {
        return all.stream().filter(p).toList();
    }

    public static class DashboardStoreImpl implements DashboardStore {
        private final DashboardMapper mapper;
        public DashboardStoreImpl(DashboardMapper mapper) { this.mapper = mapper; }
        public Dashboard save(Dashboard d) { mapper.insert(d); return d; }
        public Optional<Dashboard> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }
        public List<Dashboard> findAll() { return mapper.selectList(null); }
        public List<Dashboard> findBy(Predicate<Dashboard> p) { return filter(mapper.selectList(null), p); }
        public void deleteById(Long id) { mapper.deleteById(id); }
    }

    public static class WidgetStoreImpl implements WidgetStore {
        private final DashboardWidgetMapper mapper;
        public WidgetStoreImpl(DashboardWidgetMapper mapper) { this.mapper = mapper; }
        public DashboardWidget save(DashboardWidget w) {
            if (w.getId() == null) mapper.insert(w); else mapper.updateById(w);
            return w;
        }
        public Optional<DashboardWidget> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }
        public List<DashboardWidget> findBy(Predicate<DashboardWidget> p) {
            return filter(mapper.selectList(null), p);
        }
        public int countByDashboard(Long dashboardId) {
            return Math.toIntExact(mapper.selectCount(new QueryWrapper<DashboardWidget>()
                    .eq("dashboard_id", dashboardId)));
        }
        public void deleteById(Long id) { mapper.deleteById(id); }
        public void deleteByDashboard(Long dashboardId) {
            mapper.delete(new QueryWrapper<DashboardWidget>().eq("dashboard_id", dashboardId));
        }
    }

    public static class SnapshotStoreImpl implements SnapshotStore {
        private final WidgetSnapshotMapper mapper;
        public SnapshotStoreImpl(WidgetSnapshotMapper mapper) { this.mapper = mapper; }
        public WidgetSnapshot save(WidgetSnapshot s) { mapper.insert(s); return s; }
        public List<WidgetSnapshot> findBy(Predicate<WidgetSnapshot> p) {
            return filter(mapper.selectList(null), p);
        }
        public void deleteById(Long id) { mapper.deleteById(id); }
        public void deleteByDashboard(Long dashboardId) {
            mapper.delete(new QueryWrapper<WidgetSnapshot>().eq("dashboard_id", dashboardId));
        }
    }
}
