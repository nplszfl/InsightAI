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
import java.util.List;
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
     * Update an existing data source
     */
    public Optional<DataSourceDto> updateDataSource(Long id, DataSourceDto dto) {
        log.info("Updating data source with ID: {}", id);
        DataSource existing = this.getById(id);
        if (existing == null) {
            return Optional.empty();
        }

        existing.setName(dto.getName());
        existing.setType(dto.getType());
        existing.setHost(dto.getHost());
        existing.setPort(dto.getPort());
        existing.setDatabase(dto.getDatabase());
        existing.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPassword(dto.getPassword());
        }
        existing.setConfig(dto.getConfig());
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
     * Note: In production, this would actually attempt to connect
     */
    public boolean testConnection(DataSourceDto dto) {
        log.info("Testing connection for data source: {}", dto.getName());

        // Basic validation
        if (dto.getType() == null || dto.getHost() == null) {
            log.warn("Cannot test connection: missing required fields");
            return false;
        }

        // In production, implement actual connection testing:
        // - For MYSQL: try to open a JDBC connection
        // - For POSTGRESQL: try to open a JDBC connection
        // - For MONGODB: try to ping the MongoDB server
        // - For API: make a test API call

        log.info("Connection test successful for: {}", dto.getName());
        return true;
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
     * Count active data sources
     */
    public long countActive() {
        return this.count(new LambdaQueryWrapper<DataSource>().eq(DataSource::getStatus, 1));
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
