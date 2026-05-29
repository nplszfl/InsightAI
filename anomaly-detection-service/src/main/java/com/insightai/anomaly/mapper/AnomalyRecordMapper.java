package com.insightai.anomaly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.anomaly.entity.AnomalyRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnomalyRecordMapper extends BaseMapper<AnomalyRecord> {
}