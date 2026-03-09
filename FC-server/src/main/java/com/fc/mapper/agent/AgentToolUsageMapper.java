package com.fc.mapper.agent;

import com.fc.annotation.AutoFill;
import com.fc.entity.AgentToolUsage;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI Agent工具调用记录Mapper接口
 */
@Mapper
public interface AgentToolUsageMapper {

    /**
     * 插入工具调用记录
     * @param toolUsage 工具调用记录
     */
    @Insert("INSERT INTO agent_tool_usage(session_id, tool_name, tool_parameters, execution_time_ms, success, error_message, create_time, update_time) " +
            "VALUES(#{sessionId}, #{toolName}, #{toolParameters}, #{executionTimeMs}, #{success}, #{errorMessage}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    void insert(AgentToolUsage toolUsage);

    /**
     * 根据会话ID查询工具调用记录
     * @param sessionId 会话ID
     * @return 工具调用记录列表
     */
    @Select("SELECT * FROM agent_tool_usage WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<AgentToolUsage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据工具名称查询工具调用记录
     * @param toolName 工具名称
     * @return 工具调用记录列表
     */
    @Select("SELECT * FROM agent_tool_usage WHERE tool_name = #{toolName} ORDER BY create_time DESC LIMIT 100")
    List<AgentToolUsage> selectByToolName(@Param("toolName") String toolName);

    /**
     * 根据会话ID和工具名称查询工具调用记录
     * @param sessionId 会话ID
     * @param toolName 工具名称
     * @return 工具调用记录列表
     */
    @Select("SELECT * FROM agent_tool_usage WHERE session_id = #{sessionId} AND tool_name = #{toolName} ORDER BY create_time DESC")
    List<AgentToolUsage> selectBySessionIdAndToolName(@Param("sessionId") String sessionId, @Param("toolName") String toolName);

    /**
     * 统计工具调用次数
     * @param toolName 工具名称
     * @return 调用次数
     */
    @Select("SELECT COUNT(*) FROM agent_tool_usage WHERE tool_name = #{toolName}")
    int countByToolName(@Param("toolName") String toolName);

    /**
     * 统计工具调用成功率
     * @param toolName 工具名称
     * @return 成功率（0-1）
     */
    @Select("SELECT AVG(CASE WHEN success = TRUE THEN 1.0 ELSE 0.0 END) FROM agent_tool_usage WHERE tool_name = #{toolName}")
    Double getSuccessRateByToolName(@Param("toolName") String toolName);

    /**
     * 获取工具平均执行时间
     * @param toolName 工具名称
     * @return 平均执行时间（毫秒）
     */
    @Select("SELECT AVG(execution_time_ms) FROM agent_tool_usage WHERE tool_name = #{toolName} AND success = TRUE")
    Integer getAverageExecutionTimeByToolName(@Param("toolName") String toolName);

    /**
     * 删除指定会话的工具调用记录
     * @param sessionId 会话ID
     */
    @Delete("DELETE FROM agent_tool_usage WHERE session_id = #{sessionId}")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 删除指定时间之前的工具调用记录（用于数据清理）
     * @param beforeTime 时间阈值
     */
    @Delete("DELETE FROM agent_tool_usage WHERE create_time < #{beforeTime}")
    void deleteBeforeTime(@Param("beforeTime") String beforeTime);
}