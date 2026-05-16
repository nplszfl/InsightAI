package com.insightai.controller;

import com.insightai.common.dto.DataSourceDto;
import com.insightai.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
