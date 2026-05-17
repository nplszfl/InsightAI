package com.insightai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.common.model.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReportRepository extends BaseMapper<Report> {
    List<Report> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") Long createdBy);
    List<Report> findByTypeOrderByCreatedAtDesc(@Param("type") String type);
}
