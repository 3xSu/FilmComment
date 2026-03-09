package com.fc.service.impl.agent;

import com.fc.entity.AgentConversationHistory;
import com.fc.mapper.agent.AgentConversationHistoryMapper;
import com.fc.service.agent.AgentConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Agent对话历史服务实现类
 * 
 * 负责管理AI Agent与用户的对话历史记录，包括保存、查询、删除等操作
 */
@Slf4j
@Service
public class AgentConversationServiceImpl implements AgentConversationService {

    @Autowired
    private AgentConversationHistoryMapper agentConversationHistoryMapper;

    @Override
    @Transactional
    public void saveConversation(AgentConversationHistory conversationHistory) {
        try {
            conversationHistory.setCreateTime(LocalDateTime.now());
            conversationHistory.setUpdateTime(LocalDateTime.now());
            agentConversationHistoryMapper.insert(conversationHistory);
            log.info("保存对话历史成功，会话ID: {}, 用户ID: {}, 角色: {}", 
                    conversationHistory.getSessionId(), conversationHistory.getUserId(), 
                    conversationHistory.getMessageRole());
        } catch (Exception e) {
            log.error("保存对话历史失败，会话ID: {}, 错误: {}", 
                    conversationHistory.getSessionId(), e.getMessage(), e);
            throw new RuntimeException("保存对话历史失败", e);
        }
    }

    @Override
    public List<AgentConversationHistory> getConversationHistory(String sessionId) {
        try {
            List<AgentConversationHistory> history = agentConversationHistoryMapper.selectBySessionId(sessionId);
            log.debug("获取对话历史成功，会话ID: {}, 记录数: {}", sessionId, history.size());
            return history;
        } catch (Exception e) {
            log.error("获取对话历史失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("获取对话历史失败", e);
        }
    }

    @Override
    public List<AgentConversationHistory> getConversationHistoryByUserId(Long userId) {
        try {
            List<AgentConversationHistory> history = agentConversationHistoryMapper.selectByUserId(userId);
            log.debug("获取用户对话历史成功，用户ID: {}, 记录数: {}", userId, history.size());
            return history;
        } catch (Exception e) {
            log.error("获取用户对话历史失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("获取用户对话历史失败", e);
        }
    }

    @Override
    public List<AgentConversationHistory> getConversationHistory(String sessionId, Long userId) {
        try {
            List<AgentConversationHistory> history = agentConversationHistoryMapper.selectBySessionIdAndUserId(sessionId, userId);
            log.debug("获取对话历史成功，会话ID: {}, 用户ID: {}, 记录数: {}", sessionId, userId, history.size());
            return history;
        } catch (Exception e) {
            log.error("获取对话历史失败，会话ID: {}, 用户ID: {}, 错误: {}", sessionId, userId, e.getMessage(), e);
            throw new RuntimeException("获取对话历史失败", e);
        }
    }

    @Override
    @Transactional
    public void deleteConversationHistory(String sessionId) {
        try {
            agentConversationHistoryMapper.deleteBySessionId(sessionId);
            log.info("删除对话历史成功，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("删除对话历史失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("删除对话历史失败", e);
        }
    }

    @Override
    @Transactional
    public void deleteConversationHistoryByUserId(Long userId) {
        try {
            agentConversationHistoryMapper.deleteByUserId(userId);
            log.info("删除用户对话历史成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("删除用户对话历史失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("删除用户对话历史失败", e);
        }
    }

    @Override
    public int countConversationMessages(Long userId) {
        try {
            int count = agentConversationHistoryMapper.countByUserId(userId);
            log.debug("统计用户对话消息数量成功，用户ID: {}, 数量: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("统计用户对话消息数量失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("统计对话消息数量失败", e);
        }
    }

    @Override
    public String getLastConversationTime(Long userId) {
        try {
            String lastTime = agentConversationHistoryMapper.getLastConversationTimeByUserId(userId);
            log.debug("获取用户最近对话时间成功，用户ID: {}, 时间: {}", userId, lastTime);
            return lastTime;
        } catch (Exception e) {
            log.error("获取用户最近对话时间失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("获取最近对话时间失败", e);
        }
    }
}