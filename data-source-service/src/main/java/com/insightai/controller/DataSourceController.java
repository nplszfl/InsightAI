package com.insightai.controller;

import com.insightai.common.dto.DataSourceDto;
import com.insightai.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    public ResponseEntity<DataSourceDto> create(@RequestBody DataSourceDto dto) {
        return ResponseEntity.ok(dataSourceService.createDataSource(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataSourceDto> getById(@PathVariable Long id) {
        return dataSourceService.getDataSourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DataSourceDto>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(dataSourceService.getAllDataSources(page, size).getRecords());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DataSourceDto>> getActive() {
        return ResponseEntity.ok(dataSourceService.getActiveDataSources());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<DataSourceDto>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(dataSourceService.getDataSourcesByType(type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataSourceDto> update(@PathVariable Long id, @RequestBody DataSourceDto dto) {
        return dataSourceService.updateDataSource(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return dataSourceService.deleteDataSource(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Boolean> testConnection(@RequestBody DataSourceDto dto) {
        return ResponseEntity.ok(dataSourceService.testConnection(dto));
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<String> getConnectionUrl(@PathVariable Long id) {
        return dataSourceService.getConnectionUrlById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DataSourceDto>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(dataSourceService.searchByName(name));
    }

    @PostMapping("/{id}/refresh-config")
    public ResponseEntity<Void> refreshConfig(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newConfig = body.get("config");
        if (newConfig == null) {
            return ResponseEntity.badRequest().build();
        }
        return dataSourceService.refreshConfig(id, newConfig)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<DataSourceDto> duplicate(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return dataSourceService.duplicateDataSource(id, newName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
