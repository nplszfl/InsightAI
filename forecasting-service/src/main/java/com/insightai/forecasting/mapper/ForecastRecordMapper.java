package com.insightai.forecasting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.forecasting.entity.ForecastRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ForecastRecordMapper extends BaseMapper<ForecastRecord> {
}