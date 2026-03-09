package com.fc.agent.memory;

import com.fc.vo.user.UserPreferencesVO;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户偏好提取器
 */
@Slf4j
@Component
public class PreferenceExtractor {
    
    /**
     * 从对话历史中提取用户偏好
     * 
     * @param messages 对话消息列表
     * @return 用户偏好信息
     */
    public UserPreferencesVO extractFromConversation(List<ChatMessage> messages) {
        log.info("开始从对话历史中提取用户偏好，消息数量: {}", messages.size());
        
        UserPreferencesVO preferences = new UserPreferencesVO();
        
        if (messages == null || messages.isEmpty()) {
            log.warn("对话历史为空，无法提取用户偏好");
            return preferences;
        }
        
        // 分析对话内容，提取用户的电影类型偏好、评分偏好等
        StringBuilder conversationText = new StringBuilder();
        for (ChatMessage message : messages) {
            String messageText = extractMessageText(message);
            if (messageText != null && !messageText.trim().isEmpty()) {
                conversationText.append(messageText).append(" ");
            }
        }
        
        String fullConversation = conversationText.toString().toLowerCase();
        
        // 提取电影类型偏好
        extractGenrePreferences(fullConversation, preferences);
        
        // 提取评分偏好
        extractRatingPreferences(fullConversation, preferences);
        
        // 提取年份偏好
        extractYearPreferences(fullConversation, preferences);
        
        // 提取导演/演员偏好
        extractPersonPreferences(fullConversation, preferences);
        
        log.info("用户偏好提取完成，类型偏好: {}, 评分偏好: {}", 
                preferences.getFavoriteGenres(), preferences.getPreferredRatingRange());
        return preferences;
    }
    
    /**
     * 从不同类型的消息中提取文本内容
     * 
     * @param message 聊天消息
     * @return 消息文本内容
     */
    private String extractMessageText(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        // 根据具体消息类型使用相应的访问器方法
        if (message instanceof AiMessage) {
            AiMessage aiMessage = (AiMessage) message;
            return aiMessage.text();
        } else if (message instanceof SystemMessage) {
            SystemMessage systemMessage = (SystemMessage) message;
            return systemMessage.text();
        } else if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;
            // UserMessage使用contents()方法获取内容列表
            // 这里简化处理，只提取文本内容
            return userMessage.contents().stream()
                    .filter(content -> content.type() == dev.langchain4j.data.message.ContentType.TEXT)
                    .map(content -> ((dev.langchain4j.data.message.TextContent) content).text())
                    .findFirst()
                    .orElse("");
        } else if (message instanceof ToolExecutionResultMessage) {
            ToolExecutionResultMessage toolMessage = (ToolExecutionResultMessage) message;
            return toolMessage.text();
        } else {
            // 对于未知类型的消息，返回空字符串
            log.warn("未知消息类型: {}", message.getClass().getSimpleName());
            return "";
        }
    }
    
    /**
     * 提取电影类型偏好
     */
    private void extractGenrePreferences(String conversation, UserPreferencesVO preferences) {
        String[] genreKeywords = {"科幻", "动作", "喜剧", "爱情", "悬疑", "恐怖", "动画", "纪录片", "战争", "音乐"};
        
        for (String genre : genreKeywords) {
            if (conversation.contains(genre.toLowerCase())) {
                preferences.addPreferredGenre(genre);
            }
        }
    }
    
    /**
     * 提取评分偏好
     */
    private void extractRatingPreferences(String conversation, UserPreferencesVO preferences) {
        // 检测评分相关关键词
        if (conversation.contains("高分") || conversation.contains("评分高") || conversation.contains("8分以上")) {
            preferences.setPreferredRating("high");
        } else if (conversation.contains("低分") || conversation.contains("评分低") || conversation.contains("6分以下")) {
            preferences.setPreferredRating("low");
        } else if (conversation.contains("中等评分") || conversation.contains("7分左右")) {
            preferences.setPreferredRating("medium");
        }
    }
    
    /**
     * 提取年份偏好
     */
    private void extractYearPreferences(String conversation, UserPreferencesVO preferences) {
        // 检测年份相关关键词
        if (conversation.contains("新片") || conversation.contains("最新") || conversation.contains("202") || conversation.contains("近年")) {
            preferences.setPreferredYearRange("recent");
        } else if (conversation.contains("经典") || conversation.contains("老片") || conversation.contains("90年代") || conversation.contains("80年代")) {
            preferences.setPreferredYearRange("classic");
        }
    }
    
    /**
     * 提取导演/演员偏好
     */
    private void extractPersonPreferences(String conversation, UserPreferencesVO preferences) {
        // 这里可以集成更复杂的NLP分析
        // 暂时实现基础的关键词检测
        String[] directorKeywords = {"导演", "执导", "导演作品"};
        String[] actorKeywords = {"主演", "演员", "出演"};
        
        for (String keyword : directorKeywords) {
            if (conversation.contains(keyword)) {
                preferences.setHasDirectorPreference(true);
                break;
            }
        }
        
        for (String keyword : actorKeywords) {
            if (conversation.contains(keyword)) {
                preferences.setHasActorPreference(true);
                break;
            }
        }
    }
    
    /**
     * 从单条消息中快速提取偏好（用于实时分析）
     * 
     * @param message 单条消息
     * @return 偏好信息
     */
    public UserPreferencesVO extractFromSingleMessage(String message) {
        log.info("从单条消息中提取用户偏好: {}", message);
        
        UserPreferencesVO preferences = new UserPreferencesVO();
        
        if (message == null || message.trim().isEmpty()) {
            return preferences;
        }
        
        String lowerMessage = message.toLowerCase();
        
        // 快速提取关键信息
        extractGenrePreferences(lowerMessage, preferences);
        extractRatingPreferences(lowerMessage, preferences);
        extractYearPreferences(lowerMessage, preferences);
        
        return preferences;
    }
}