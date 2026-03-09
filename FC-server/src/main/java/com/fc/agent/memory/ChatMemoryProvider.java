package com.fc.agent.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天记忆管理配置类
 */
@Slf4j
@Configuration
public class ChatMemoryProvider {
    
    /**
     * 配置ChatMemory Bean
     * 
     * @return ChatMemory实例
     */
    @Bean
    public ChatMemory chatMemory() {
        log.info("初始化ChatMemory，最大消息数: 10");
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }
}