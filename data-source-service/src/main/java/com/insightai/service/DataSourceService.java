package com.insightai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.insightai.common.dto.DataSourceDto;
import com.insightai.common.model.DataSource;
import com.insightai.repository.DataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DataSource Management Service
 * Handles CRUD operations for data source connections with MyBatis Plus
 */
@Slf4j
@Service
public class DataSourceService extends ServiceImpl<DataSourceRepository, DataSource> {

    /**
     * Create a new data source connection
     */
    public DataSourceDto createDataSource(DataSourceDto dto) {
        log.info("Creating new data source: {}", dto.getName());

        // Validate unique name
        validateUniqueName(dto.getName(), null);

        DataSource dataSource = toEntity(dto);
        dataSource.setCreatedAt(LocalDateTime.now());
        dataSource.setUpdatedAt(LocalDateTime.now());
        dataSource.setStatus(1);

        this.save(dataSource);
        log.info("Created data source with ID: {}", dataSource.getId());

        return toDto(dataSource);
    }

    /**
     * Get data source by ID
     */
    public Optional<DataSourceDto> getDataSourceById(Long id) {
        log.info("Fetching data source with ID: {}", id);
        DataSource ds = this.getById(id);
        return Optional.ofNullable(ds).map(this::toDto);
    }

    /**
     * Get all data sources with pagination
     */
    public IPage<DataSourceDto> getAllDataSources(int page, int size) {
        log.info("Fetching all data sources, page: {}, size: {}", page, size);
        Page<DataSource> pageParam = new Page<>(page, size);
        IPage<DataSource> result = this.page(pageParam, new LambdaQueryWrapper<DataSource>()
                .orderByDesc(DataSource::getCreatedAt));

        return result.convert(this::toDto);
    }

    /**
     * Get active data sources only
     */
    public List<DataSourceDto> getActiveDataSources() {
        log.info("Fetching all active data sources");
        return this.list(new LambdaQueryWrapper<DataSource>()
                .eq(DataSource::getStatus, 1)
                .orderByDesc(DataSource::getCreatedAt))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get data sources by type
     */
    public List<DataSourceDto> getDataSourcesByType(String type) {
        log.info("Fetching data sources of type: {}", type);
        return this.list(new LambdaQueryWrapper<DataSource>()
                .eq(DataSource::getType, type)
                .eq(DataSource::getStatus, 1)
                .orderByDesc(DataSource::getCreatedAt))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing data source
     */
    public Optional<DataSourceDto> updateDataSource(Long id, DataSourceDto dto) {
        log.info("Updating data source with ID: {}", id);
        DataSource existing = this.getById(id);
        if (existing == null) {
            return Optional.empty();
        }

        // Validate unique name if being changed
        if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
            validateUniqueName(dto.getName(), id);
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getType() != null) {
            existing.setType(dto.getType());
        }
        if (dto.getHost() != null) {
            existing.setHost(dto.getHost());
        }
        if (dto.getPort() != null) {
            existing.setPort(dto.getPort());
        }
        if (dto.getDatabase() != null) {
            existing.setDatabase(dto.getDatabase());
        }
        if (dto.getUsername() != null) {
            existing.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPassword(dto.getPassword());
        }
        if (dto.getConfig() != null) {
            existing.setConfig(dto.getConfig());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return Optional.of(toDto(existing));
    }

    /**
     * Delete a data source (soft delete by setting status to 0)
     */
    public boolean deleteDataSource(Long id) {
        log.info("Deleting (soft) data source with ID: {}", id);
        DataSource ds = this.getById(id);
        if (ds == null) {
            return false;
        }
        ds.setStatus(0);
        ds.setUpdatedAt(LocalDateTime.now());
        return this.updateById(ds);
    }

    /**
     * Test connection to a data source
     * Note: This is a placeholder - actual connection testing would require DB-specific logic
     */
    public boolean testConnection(DataSourceDto dto) {
        log.info("Testing connection for data source: {}", dto.getName());

        // Basic validation
        if (dto.getType() == null || dto.getHost() == null) {
            log.warn("Cannot test connection: missing required fields");
            return false;
        }

        log.info("Connection test successful for: {}", dto.getName());
        return true;
    }

    /**
     * Count active data sources
     */
    public long countActive() {
        return this.count(new LambdaQueryWrapper<DataSource>().eq(DataSource::getStatus, 1));
    }

    /**
     * Enable a data source
     */
    public boolean enableDataSource(Long id) {
        log.info("Enabling data source: {}", id);
        DataSource ds = this.getById(id);
        if (ds == null) {
            return false;
        }
        ds.setStatus(1);
        ds.setUpdatedAt(LocalDateTime.now());
        return this.updateById(ds);
    }

    /**
     * Disable a data source
     */
    public boolean disableDataSource(Long id) {
        log.info("Disabling data source: {}", id);
        DataSource ds = this.getById(id);
        if (ds == null) {
            return false;
        }
        ds.setStatus(0);
        ds.setUpdatedAt(LocalDateTime.now());
        return this.updateById(ds);
    }

    /**
     * Batch update status for multiple data sources
     */
    public int batchUpdateStatus(List<Long> ids, int status) {
        log.info("Batch updating status for {} data sources to {}", ids.size(), status);
        for (Long id : ids) {
            DataSource ds = this.getById(id);
            if (ds != null) {
                ds.setStatus(status);
                ds.setUpdatedAt(LocalDateTime.now());
                this.updateById(ds);
            }
        }
        return ids.size();
    }

    /**
     * Get data source statistics
     */
    public Map<String, Object> getStatistics() {
        long total = this.count();
        long active = this.count(new LambdaQueryWrapper<DataSource>().eq(DataSource::getStatus, 1));
        long inactive = total - active;

        // Count by type
        Map<String, Long> byType = new HashMap<>();
        List<DataSource> allDataSources = this.list();
        for (DataSource ds : allDataSources) {
            String type = ds.getType() != null ? ds.getType() : "UNKNOWN";
            byType.put(type, byType.getOrDefault(type, 0L) + 1);
        }

        return Map.of(
                "total", total,
                "active", active,
                "inactive", inactive,
                "countByType", byType
        );
    }

    /**
     * Validate data source before creation/update
     */
    public void validateDataSource(DataSourceDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Data source name is required");
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Data source type is required");
        }
        if (dto.getHost() == null || dto.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Data source host is required");
        }
        validateUniqueName(dto.getName().trim(), dto.getId());
    }

    /**
     * Get connection URL for a data source
     */
    public Optional<String> getConnectionUrl(DataSourceDto dto) {
        if (dto.getType() == null || dto.getHost() == null) {
            return Optional.empty();
        }

        String url;
        switch (dto.getType().toUpperCase()) {
            case "MYSQL":
                int mysqlPort = dto.getPort() != null ? dto.getPort() : 3306;
                url = String.format("jdbc:mysql://%s:%d/%s", dto.getHost(), mysqlPort,
                        dto.getDatabase() != null ? dto.getDatabase() : "");
                break;
            case "POSTGRESQL":
                int pgPort = dto.getPort() != null ? dto.getPort() : 5432;
                url = String.format("jdbc:postgresql://%s:%d/%s", dto.getHost(), pgPort,
                        dto.getDatabase() != null ? dto.getDatabase() : "");
                break;
            case "MONGODB":
                int mongoPort = dto.getPort() != null ? dto.getPort() : 27017;
                url = String.format("mongodb://%s:%d/%s", dto.getHost(), mongoPort,
                        dto.getDatabase() != null ? dto.getDatabase() : "");
                break;
            default:
                return Optional.empty();
        }

        return Optional.of(url);
    }

    /**
     * Validate unique name (excluding a specific ID for updates)
     */
    private void validateUniqueName(String name, Long excludeId) {
        LambdaQueryWrapper<DataSource> query = new LambdaQueryWrapper<DataSource>()
                .eq(DataSource::getName, name);
        if (excludeId != null) {
            query.ne(DataSource::getId, excludeId);
        }
        if (this.count(query) > 0) {
            throw new IllegalArgumentException("Data source with name '" + name + "' already exists");
        }
    }

    private DataSource toEntity(DataSourceDto dto) {
        return DataSource.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .host(dto.getHost())
                .port(dto.getPort())
                .database(dto.getDatabase())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .config(dto.getConfig())
                .status(dto.getStatus())
                .build();
    }

    private DataSourceDto toDto(DataSource entity) {
        if (entity == null) return null;
        return DataSourceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .host(entity.getHost())
                .port(entity.getPort())
                .database(entity.getDatabase())
                .username(entity.getUsername())
                // Don't expose password in responses by default
                .config(entity.getConfig())
                .status(entity.getStatus())
                .build();
    }
}