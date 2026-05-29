package com.insightai.forecasting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.forecasting.entity.SeasonalityDetection;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeasonalityDetectionMapper extends BaseMapper<SeasonalityDetection> {
}