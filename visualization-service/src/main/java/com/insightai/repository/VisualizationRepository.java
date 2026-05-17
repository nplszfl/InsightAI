package com.insightai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.common.model.Visualization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VisualizationRepository extends BaseMapper<Visualization> {
    List<Visualization> findByReportIdOrderByPosition(@Param("reportId") Long reportId);
}
