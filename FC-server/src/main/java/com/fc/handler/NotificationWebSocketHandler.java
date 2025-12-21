package com.fc.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fc.context.BaseContext;
import com.fc.vo.websocket.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket连接建立成功: userId={}, sessionId={}", userId, session.getId());

            // 发送连接成功消息
            sendMessage(userId, WebSocketMessage.builder()
                    .type("CONNECT_SUCCESS")
                    .data("连接成功")
                    .timestamp(LocalDateTime.now())
                    .build());
        } else {
            log.warn("WebSocket连接建立失败: 无法获取用户ID");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * 处理文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            log.debug("收到WebSocket消息: {}", wsMessage);

            // 处理心跳消息
            if ("PING".equals(wsMessage.getType())) {
                handlePingMessage(session, wsMessage);
            }
            // 处理确认消息
            else if ("ACK".equals(wsMessage.getType())) {
                handleAckMessage(wsMessage);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", message.getPayload(), e);
        }
    }

    /**
     * 处理连接错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket连接关闭: userId={}, status={}", userId, status);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Long userId, WebSocketMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(messageJson));
                log.debug("发送WebSocket消息成功: userId={}, type={}", userId, message.getType());
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: userId={}", userId, e);
                // 移除无效会话
                userSessions.remove(userId);
            }
        }
    }

    /**
     * 处理心跳消息
     */
    private void handlePingMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            WebSocketMessage pongMessage = WebSocketMessage.builder()
                    .type("PONG")
                    .data("pong")
                    .timestamp(LocalDateTime.now())
                    .messageId(message.getMessageId())
                    .build();
            sendMessageToUser(userId, pongMessage);
        }
    }

    /**
     * 处理确认消息
     */
    private void handleAckMessage(WebSocketMessage message) {
        log.debug("收到ACK确认: messageId={}", message.getMessageId());
        // 可以在这里实现消息确认逻辑
    }

    /**
     * 从会话中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 从查询参数中获取token并解析用户ID
            String query = session.getUri().getQuery();
            if (query != null && query.contains("token=")) {
                String token = query.substring(query.indexOf("token=") + 6);
                // 这里需要实现token解析逻辑，获取用户ID
                // 暂时返回1，实际项目中需要根据token解析
                return 1L;
            }
        } catch (Exception e) {
            log.error("从WebSocket会话获取用户ID失败", e);
        }
        return null;
    }

    /**
     * 发送消息（内部使用）
     */
    private void sendMessage(Long userId, WebSocketMessage message) {
        sendMessageToUser(userId, message);
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastToAll(WebSocketMessage message) {
        if (userSessions.isEmpty()) {
            return;
        }

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化广播消息失败", e);
            return;
        }

        TextMessage textMessage = new TextMessage(messageJson);
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("广播消息失败: userId={}", userId, e);
                    // 移除无效会话
                    userSessions.remove(userId);
                }
            }
        });

        log.debug("广播消息成功: type={}, 接收用户数={}", message.getType(), userSessions.size());
    }

}
