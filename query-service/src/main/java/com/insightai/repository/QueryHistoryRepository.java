package com.insightai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.common.model.QueryHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QueryHistoryRepository extends BaseMapper<QueryHistory> {
    List<QueryHistory> findByDataSourceIdOrderByCreatedAtDesc(@Param("dataSourceId") Long dataSourceId);
}
