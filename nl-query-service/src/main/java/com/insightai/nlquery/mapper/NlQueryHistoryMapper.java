package com.insightai.nlquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.nlquery.entity.NlQueryHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NlQueryHistoryMapper extends BaseMapper<NlQueryHistory> {
}