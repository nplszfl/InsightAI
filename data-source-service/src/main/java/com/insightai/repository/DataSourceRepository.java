package com.insightai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insightai.common.model.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataSourceRepository extends BaseMapper<DataSource> {

    @Select("SELECT * FROM data_source WHERE status = 1 ORDER BY created_at DESC")
    IPage<DataSource> findActive(Page<DataSource> page);

    @Select("SELECT * FROM data_source WHERE type = #{type} ORDER BY created_at DESC")
    IPage<DataSource> findByType(Page<DataSource> page, @Param("type") String type);

    @Select("SELECT COUNT(*) FROM data_source WHERE status = 1")
    long countActive();
}
