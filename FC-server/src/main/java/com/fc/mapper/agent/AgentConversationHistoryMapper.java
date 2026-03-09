package com.fc.mapper.agent;

import com.fc.annotation.AutoFill;
import com.fc.entity.AgentConversationHistory;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI Agent对话历史Mapper接口
 */
@Mapper
public interface AgentConversationHistoryMapper {

    /**
     * 插入对话历史记录
     * @param conversationHistory 对话历史记录
     */
    @Insert("INSERT INTO agent_conversation_history(session_id, user_id, message_text, message_role, create_time, update_time) " +
            "VALUES(#{sessionId}, #{userId}, #{messageText}, #{messageRole}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    void insert(AgentConversationHistory conversationHistory);

    /**
     * 根据会话ID查询对话历史
     * @param sessionId 会话ID
     * @return 对话历史列表
     */
    @Select("SELECT * FROM agent_conversation_history WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<AgentConversationHistory> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID查询对话历史
     * @param userId 用户ID
     * @return 对话历史列表
     */
    @Select("SELECT * FROM agent_conversation_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 100")
    List<AgentConversationHistory> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID和用户ID查询对话历史
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 对话历史列表
     */
    @Select("SELECT * FROM agent_conversation_history WHERE session_id = #{sessionId} AND user_id = #{userId} ORDER BY create_time ASC")
    List<AgentConversationHistory> selectBySessionIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    /**
     * 删除指定会话的对话历史
     * @param sessionId 会话ID
     */
    @Delete("DELETE FROM agent_conversation_history WHERE session_id = #{sessionId}")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 删除指定用户的对话历史
     * @param userId 用户ID
     */
    @Delete("DELETE FROM agent_conversation_history WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 统计用户对话消息数量
     * @param userId 用户ID
     * @return 消息数量
     */
    @Select("SELECT COUNT(*) FROM agent_conversation_history WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 获取用户最近一次对话时间
     * @param userId 用户ID
     * @return 最近对话时间
     */
    @Select("SELECT MAX(create_time) FROM agent_conversation_history WHERE user_id = #{userId}")
    String getLastConversationTimeByUserId(@Param("userId") Long userId);
}