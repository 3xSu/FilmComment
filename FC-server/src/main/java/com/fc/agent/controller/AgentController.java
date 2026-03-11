package com.fc.agent.controller;

import com.fc.service.agent.AssistantService;
import com.fc.context.BaseContext;
import com.fc.dto.agent.AgentRequest;
import com.fc.dto.agent.AgentResponse;
import com.fc.entity.AgentConversationHistory;
import com.fc.exception.AgentModelTimeoutException;
import com.fc.exception.AgentServiceException;
import com.fc.exception.AgentToolExecutionException;
import com.fc.exception.UserNoViewingDataException;
import com.fc.result.Result;
import com.fc.service.agent.AgentConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI Agent控制器
 * 提供与AI Agent对话的统一入口
 */
@Slf4j
@RestController
@RequestMapping("/agent")
@Tag(name = "AI Agent接口")
@RequiredArgsConstructor
public class AgentController {
    
    private final AssistantService movieAssistant;
    private final AgentConversationService agentConversationService;
    
    /**
     * 与电影推荐Agent对话
     * 
     * @param message 用户消息
     * @return Agent响应结果
     */
    @PostMapping("/chat")
    @Operation(summary = "与电影推荐Agent对话")
    public Result<String> chat(@RequestParam String message) {
        Long userId = BaseContext.getCurrentId();
        String sessionId = UUID.randomUUID().toString();
        log.info("AI助手对话请求，用户ID: {}, 会话ID: {}, 消息: {}", userId, sessionId, message);
        
        try {
            // 输入验证
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            if (message.length() > 500) {
                return Result.error("消息长度不能超过500个字符");
            }
            
            // 保存用户消息到数据库
            AgentConversationHistory userMessage = AgentConversationHistory.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageText(message)
                    .messageRole("user")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            agentConversationService.saveConversation(userMessage);
            
            String response = movieAssistant.chat(message);
            
            // 保存助手响应到数据库
            AgentConversationHistory assistantMessage = AgentConversationHistory.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageText(response)
                    .messageRole("assistant")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            agentConversationService.saveConversation(assistantMessage);
            
            log.info("AI助手响应完成，用户ID: {}, 会话ID: {}, 响应长度: {}", userId, sessionId, response.length());
            return Result.success(response);
        } catch (AgentToolExecutionException e) {
            log.warn("AI助手工具调用失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            return Result.error("抱歉，工具调用失败，请稍后重试");
        } catch (AgentModelTimeoutException e) {
            log.error("AI助手模型响应超时，用户ID: {}, 错误: {}", userId, e.getMessage());
            return Result.error("AI助手响应超时，请稍后重试");
        } catch (AgentServiceException e) {
            log.error("AI助手服务异常，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return Result.error("AI助手暂时无法处理您的请求，请稍后重试");
        } catch (Exception e) {
            log.error("AI助手对话处理异常，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return Result.error("系统繁忙，请稍后重试");
        }
    }
    
    /**
     * 与电影推荐Agent对话（支持会话管理）
     * 
     * @param request Agent请求参数
     * @return Agent响应结果
     */
    @PostMapping("/chat/session")
    @Operation(summary = "与电影推荐Agent对话（支持会话管理）")
    public Result<AgentResponse> chatWithSession(@RequestBody AgentRequest request) {
        Long userId = BaseContext.getCurrentId();
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        log.info("AI助手会话对话请求，用户ID: {}, 会话ID: {}, 消息: {}", 
                userId, sessionId, request.getMessage());
        
        try {
            // 输入验证
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            if (request.getMessage().length() > 500) {
                return Result.error("消息长度不能超过500个字符");
            }
            
            // 保存用户消息到数据库
            AgentConversationHistory userMessage = AgentConversationHistory.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageText(request.getMessage())
                    .messageRole("user")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            agentConversationService.saveConversation(userMessage);
            
            String response = movieAssistant.chat(request.getMessage());
            
            // 保存助手响应到数据库
            AgentConversationHistory assistantMessage = AgentConversationHistory.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageText(response)
                    .messageRole("assistant")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            agentConversationService.saveConversation(assistantMessage);
            
            AgentResponse agentResponse = new AgentResponse();
            agentResponse.setSessionId(sessionId);
            agentResponse.setResponse(response);
            agentResponse.setStatus("success");
            
            log.info("AI助手会话响应完成，用户ID: {}, 会话ID: {}, 响应长度: {}", 
                    userId, sessionId, response.length());
            return Result.success(agentResponse);
        } catch (AgentToolExecutionException e) {
            log.warn("AI助手工具调用失败，用户ID: {}, 会话ID: {}, 错误: {}", 
                    userId, sessionId, e.getMessage());
            
            // 分析工具调用失败的具体原因，提供更友好的错误信息
            String errorMessage = analyzeToolExecutionError(e);
            
            // 保存友好的错误响应到数据库，避免存储工具调用参数
            saveToolErrorResponse(sessionId, userId, null, errorMessage);
            
            AgentResponse agentResponse = new AgentResponse();
            agentResponse.setSessionId(sessionId);
            agentResponse.setResponse(errorMessage);
            agentResponse.setStatus("error");
            
            return Result.success(agentResponse);
        } catch (AgentModelTimeoutException e) {
            log.error("AI助手模型响应超时，用户ID: {}, 会话ID: {}, 错误: {}", 
                    userId, sessionId, e.getMessage());
            
            AgentResponse agentResponse = new AgentResponse();
            agentResponse.setSessionId(sessionId);
            agentResponse.setResponse("AI助手响应超时，请稍后重试");
            agentResponse.setStatus("error");
            
            return Result.success(agentResponse);
        } catch (AgentServiceException e) {
            log.error("AI助手服务异常，用户ID: {}, 会话ID: {}, 错误: {}", 
                    userId, sessionId, e.getMessage(), e);
            
            AgentResponse agentResponse = new AgentResponse();
            agentResponse.setSessionId(sessionId);
            agentResponse.setResponse("AI助手暂时无法处理您的请求，请稍后重试");
            agentResponse.setStatus("error");
            
            return Result.success(agentResponse);
        } catch (Exception e) {
            log.error("AI助手会话对话处理异常，用户ID: {}, 会话ID: {}, 错误: {}", 
                    userId, sessionId, e.getMessage(), e);
            
            AgentResponse agentResponse = new AgentResponse();
            agentResponse.setSessionId(sessionId);
            agentResponse.setResponse("系统繁忙，请稍后重试");
            agentResponse.setStatus("error");
            
            return Result.success(agentResponse);
        }
    }
    
    /**
     * 分析工具调用失败的具体原因，提供友好的错误信息
     * 
     * @param e Agent工具执行异常
     * @return 友好的错误提示信息
     */
    private String analyzeToolExecutionError(AgentToolExecutionException e) {
        String errorMessage = e.getMessage();
        
        // 根据异常信息分析具体原因
        if (errorMessage != null) {
            if (errorMessage.contains("用户无观影数据") || errorMessage.contains("UserNoViewingDataException")) {
                return "您还没有观影历史数据，无法基于您的观影偏好进行推荐。建议您先观看一些电影，系统会根据您的观影记录为您提供更精准的推荐";
            } else if (errorMessage.contains("数据库连接") || errorMessage.contains("连接池")) {
                return "系统暂时无法访问数据服务，请稍后重试。如果问题持续存在，请联系系统管理员";
            } else if (errorMessage.contains("超时") || errorMessage.contains("timeout")) {
                return "推荐服务响应超时，可能是由于网络问题或系统繁忙。请稍后重试";
            }
        }
        
        // 默认错误信息
        return "抱歉，AI助手在执行推荐时遇到问题。这可能是因为数据服务暂时不可用，请稍后重试";
    }
    
    /**
     * 处理工具调用失败时的数据库存储逻辑
     * 避免将工具调用参数直接存储为响应内容
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param originalResponse 原始响应（可能是工具调用参数）
     * @param errorMessage 友好的错误信息
     */
    private void saveToolErrorResponse(String sessionId, Long userId, String originalResponse, String errorMessage) {
        try {
            // 检查原始响应是否是工具调用参数（JSON格式）
            if (originalResponse != null && 
                (originalResponse.contains("\"name\"") && originalResponse.contains("\"parameters\""))) {
                log.warn("检测到工具调用参数被错误存储为响应，会话ID: {}, 用户ID: {}", sessionId, userId);
                
                // 使用友好的错误信息替代工具调用参数
                AgentConversationHistory assistantMessage = AgentConversationHistory.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .messageText(errorMessage)
                        .messageRole("assistant")
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                agentConversationService.saveConversation(assistantMessage);
            } else {
                // 正常存储响应
                AgentConversationHistory assistantMessage = AgentConversationHistory.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .messageText(errorMessage)
                        .messageRole("assistant")
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                agentConversationService.saveConversation(assistantMessage);
            }
        } catch (Exception ex) {
            log.error("保存工具错误响应失败，会话ID: {}, 用户ID: {}, 错误: {}", sessionId, userId, ex.getMessage());
        }
    }
}