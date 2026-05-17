package com.insightai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.common.model.QueryCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface QueryCacheRepository extends BaseMapper<QueryCache> {
    void deleteExpired(@Param("now") LocalDateTime now);
}
