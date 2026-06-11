package com.insightai.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.dashboard.entity.Dashboard;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper extends BaseMapper<Dashboard> {
}
