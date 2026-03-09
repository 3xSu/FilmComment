package com.fc.agent.config;

import com.fc.agent.service.Assistant;
import com.fc.agent.tools.HistoryRecommendTool;
import com.fc.agent.tools.MCPToolAdapter;
import com.fc.agent.tools.MovieSearchTool;
import com.fc.utils.PromptManager;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Agent配置类
 */
@Slf4j
@Configuration
public class AgentConfig {
    
    @Value("${agent.ollama.base-url}")
    private String ollamaBaseUrl;
    
    @Value("${agent.ollama.model-name}")
    private String ollamaModelName;
    
    /**
     * 配置ChatModel Bean
     * 
     * @return ChatModel实例
     */
    @Bean
    public ChatModel chatModel() {
        log.info("初始化Ollama ChatModel，baseUrl: {}, modelName: {}", ollamaBaseUrl, ollamaModelName);
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .build();
    }
    
    /**
     * 配置默认聊天记忆
     * 
     * @return ChatMemory实例
     */
    @Bean
    public ChatMemory defaultChatMemory() {
        log.info("初始化默认聊天记忆，保留最近10条消息");
        return dev.langchain4j.memory.chat.MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }
    
    /**
     * 配置AI助手Bean（使用AiServices模式）
     * 使用用户上下文感知的工具类，解决用户ID传递问题
     * 
     * @param model ChatModel实例
     * @param chatMemory 聊天记忆
     * @param movieSearchTool 电影搜索工具
     * @param userAwareHistoryRecommendTool 用户上下文感知的历史推荐工具
     * @param mcpToolAdapter MCP工具适配器
     * @param promptManager 提示词管理器
     * @return AI助手实例
     */
    @Bean
    public Assistant movieAssistant(ChatModel model, ChatMemory chatMemory, 
                                   MovieSearchTool movieSearchTool, 
                                   HistoryRecommendTool userAwareHistoryRecommendTool, 
                                   MCPToolAdapter mcpToolAdapter,
                                   PromptManager promptManager) {
        log.info("初始化AI助手，集成用户上下文感知工具和优化提示词");
        
        return AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(movieSearchTool, userAwareHistoryRecommendTool, mcpToolAdapter)
                .chatMemory(chatMemory)
                .systemMessage(promptManager.SYSTEM_PROMPT)
                .build();
    }
    

}