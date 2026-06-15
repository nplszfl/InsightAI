package com.insightai.nlquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insightai.nlquery.entity.ConversationContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationContextMapper extends BaseMapper<ConversationContext> {

    /**
     * Fetch the most recent N context rows for a given session, ordered by createdAt DESC.
     * Returned in chronological order (oldest first) for natural context consumption.
     */
    @Select("SELECT * FROM conversation_context " +
            "WHERE session_id = #{sessionId} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    List<ConversationContext> selectRecentBySession(String sessionId, int limit);
}
