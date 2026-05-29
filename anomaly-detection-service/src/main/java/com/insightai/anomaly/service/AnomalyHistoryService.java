package com.insightai.anomaly.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.insightai.anomaly.entity.AnomalyRecord;
import com.insightai.anomaly.mapper.AnomalyRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyHistoryService {
    
    private final AnomalyRecordMapper anomalyRecordMapper;
    
    public List<AnomalyRecord> getHistory(String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<AnomalyRecord> queryWrapper = new QueryWrapper<>();
        
        if (metricName != null && !metricName.isEmpty()) {
            queryWrapper.eq("metric_name", metricName);
        }
        
        if (startTime != null) {
            queryWrapper.ge("detection_time", startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le("detection_time", endTime);
        }
        
        queryWrapper.orderByDesc("detection_time");
        return anomalyRecordMapper.selectList(queryWrapper);
    }
    
    public boolean acknowledgeAnomaly(Long id, String acknowledgedBy) {
        AnomalyRecord record = anomalyRecordMapper.selectById(id);
        if (record != null) {
            record.setAcknowledged(true);
            record.setAcknowledgedBy(acknowledgedBy);
            record.setAcknowledgedAt(LocalDateTime.now());
            return anomalyRecordMapper.updateById(record) > 0;
        }
        return false;
    }
    
    public long getUnacknowledgedCount(String metricName) {
        QueryWrapper<AnomalyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("acknowledged", false);
        if (metricName != null && !metricName.isEmpty()) {
            queryWrapper.eq("metric_name", metricName);
        }
        return anomalyRecordMapper.selectCount(queryWrapper);
    }
}