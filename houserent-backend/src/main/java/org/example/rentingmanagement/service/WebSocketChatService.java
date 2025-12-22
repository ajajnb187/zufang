package org.example.rentingmanagement.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.rentingmanagement.entity.ChatMessage;
import org.example.rentingmanagement.entity.WebsocketSession;
import org.example.rentingmanagement.mapper.ChatMessageMapper;
import org.example.rentingmanagement.mapper.WebsocketSessionMapper;
import org.example.rentingmanagement.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket实时聊天服务
 * 基于SpringBoot 3.2 + WebSocket最新实现方案
 */
@Component
@ServerEndpoint("/websocket/chat/{userId}")
public class WebSocketChatService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatService.class);

    /**
     * 存储在线用户的Session信息
     * Key: userId, Value: WebSocket Session
     */
    private static final Map<Long, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    /**
     * 注入Mapper和Service（需要使用静态方式注入）
     */
    private static ChatMessageMapper chatMessageMapper;
    private static WebsocketSessionMapper websocketSessionMapper;
    private static SensitiveWordService sensitiveWordService;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper chatMessageMapper) {
        WebSocketChatService.chatMessageMapper = chatMessageMapper;
    }

    @Autowired
    public void setWebsocketSessionMapper(WebsocketSessionMapper websocketSessionMapper) {
        WebSocketChatService.websocketSessionMapper = websocketSessionMapper;
    }

    @Autowired
    public void setSensitiveWordService(SensitiveWordService sensitiveWordService) {
        WebSocketChatService.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(@PathParam("userId") Long userId, Session session) {
        try {
            // 如果用户已经在线，先关闭旧连接
            if (ONLINE_SESSIONS.containsKey(userId)) {
                Session oldSession = ONLINE_SESSIONS.get(userId);
                if (oldSession.isOpen()) {
                    oldSession.close();
                }
            }

            // 保存新连接
            ONLINE_SESSIONS.put(userId, session);

            // 记录到数据库（使用try-catch避免数据库错误影响WebSocket连接）
            try {
                // 先按user_id删除旧记录（解决唯一索引冲突）
                websocketSessionMapper.deleteByUserId(userId);
                
                WebsocketSession wsSession = new WebsocketSession();
                String sessionId = java.util.UUID.randomUUID().toString();
                wsSession.setSessionId(sessionId);
                wsSession.setUserId(userId);
                wsSession.setStatus("online");
                wsSession.setLastHeartbeat(LocalDateTime.now());
                wsSession.setCreatedAt(LocalDateTime.now());
                wsSession.setUpdatedAt(LocalDateTime.now());
                websocketSessionMapper.insert(wsSession);
            } catch (Exception dbError) {
                log.warn("记录WebSocket会话到数据库失败，但不影响连接: {}", dbError.getMessage());
            }

            log.info("用户{}连接WebSocket成功，SessionId: {}", userId, session.getId());

            // 发送连接成功消息
            sendMessageToUser(userId, createSystemMessage("连接成功"));

        } catch (Exception e) {
            log.error("WebSocket连接异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId, Session session) {
        try {
            // 移除在线用户
            ONLINE_SESSIONS.remove(userId);

            // 更新数据库状态
            websocketSessionMapper.updateStatus(session.getId(), "offline");

            log.info("用户{}断开WebSocket连接，SessionId: {}", userId, session.getId());

        } catch (Exception e) {
            log.error("WebSocket关闭异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(@PathParam("userId") Long userId, String message, Session session) {
        try {
            log.info("收到用户{}的消息: {}", userId, message);

            // 解析消息
            ChatMessageDTO messageDTO = JSON.parseObject(message, ChatMessageDTO.class);

            // 处理不同类型的消息
            switch (messageDTO.getType()) {
                case "chat":
                    handleChatMessage(userId, messageDTO);
                    break;
                case "heartbeat":
                    handleHeartbeat(userId, session.getId());
                    break;
                case "read":
                    handleReadMessage(userId, messageDTO.getSenderId());
                    break;
                default:
                    log.warn("未知消息类型: {}", messageDTO.getType());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(@PathParam("userId") Long userId, Session session, Throwable error) {
        log.error("用户{}WebSocket发生错误: {}", userId, error.getMessage(), error);
    }

    /**
     * 处理聊天消息
     * 实现双层内容审核：
     * 1. 实时敏感词过滤（DFA算法） - 前端显示过滤后内容
     * 2. 数据库保存原始内容 - 供AI定时审核
     */
    private void handleChatMessage(Long senderId, ChatMessageDTO messageDTO) {
        try {
            // 生成会话ID（较小的ID在前）
            String sessionId = generateSessionId(senderId, messageDTO.getReceiverId());

            String originalContent = messageDTO.getContent();
            String filteredContent = originalContent;
            boolean hasSensitive = false;
            String sensitiveWordsJson = null;

            // 敏感词过滤（仅对文本消息进行过滤）
            if ("text".equals(messageDTO.getMessageType()) || messageDTO.getMessageType() == null) {
                if (sensitiveWordService != null) {
                    SensitiveWordService.FilterResult filterResult = sensitiveWordService.filterContent(originalContent);
                    filteredContent = filterResult.getFilteredContent();
                    hasSensitive = filterResult.isHasSensitive();
                    if (hasSensitive) {
                        sensitiveWordsJson = JSON.toJSONString(filterResult.getSensitiveWords());
                        log.warn("用户{}发送的消息包含敏感词: {}", senderId, filterResult.getSensitiveWords());
                    }
                }
            }

            // 保存消息到数据库（原始内容 + 过滤后内容）
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setSenderId(senderId);
            chatMessage.setReceiverId(messageDTO.getReceiverId());
            chatMessage.setHouseId(messageDTO.getHouseId());
            chatMessage.setMessageType(messageDTO.getMessageType() != null ? messageDTO.getMessageType() : "text");
            chatMessage.setContent(originalContent);          // 原始内容（用于AI审核）
            chatMessage.setFilteredContent(filteredContent);  // 过滤后内容（用于显示）
            chatMessage.setHasSensitive(hasSensitive);        // 是否包含敏感词
            chatMessage.setSensitiveWords(sensitiveWordsJson); // 检测到的敏感词列表
            chatMessage.setIsRead(false);
            chatMessageMapper.insert(chatMessage);

            // 构造转发消息（使用过滤后的内容）
            ChatMessageResponse response = new ChatMessageResponse();
            response.setMessageId(chatMessage.getMessageId());
            response.setSessionId(sessionId);
            response.setSenderId(senderId);
            response.setReceiverId(messageDTO.getReceiverId());
            response.setHouseId(messageDTO.getHouseId());
            response.setMessageType(chatMessage.getMessageType());
            response.setContent(filteredContent);  // 发送过滤后的内容给前端显示
            response.setCreatedAt(chatMessage.getCreatedAt());
            response.setType("message");

            // 发送给接收者
            sendMessageToUser(messageDTO.getReceiverId(), JSON.toJSONString(response));

            // 发送给发送者（确认消息）
            response.setType("sent");
            sendMessageToUser(senderId, JSON.toJSONString(response));

            log.info("聊天消息处理完成: {} -> {}, 敏感词过滤: {}", senderId, messageDTO.getReceiverId(), hasSensitive);

        } catch (Exception e) {
            log.error("处理聊天消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(Long userId, String sessionId) {
        try {
            websocketSessionMapper.updateHeartbeat(sessionId);
            sendMessageToUser(userId, createSystemMessage("heartbeat"));
        } catch (Exception e) {
            log.error("处理心跳消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理已读消息
     */
    private void handleReadMessage(Long userId, Long senderId) {
        try {
            chatMessageMapper.markMessagesAsRead(userId, senderId);
            log.info("标记消息已读: 接收者={}, 发送者={}", userId, senderId);
        } catch (Exception e) {
            log.error("处理已读消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public static boolean sendMessageToUser(Long userId, String message) {
        try {
            Session session = ONLINE_SESSIONS.get(userId);
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendText(message);
                return true;
            } else {
                log.debug("用户{}不在线，无法发送消息", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("发送消息失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public static void broadcastMessage(String message) {
        ONLINE_SESSIONS.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("广播消息失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return ONLINE_SESSIONS.size();
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        Session session = ONLINE_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    /**
     * 创建系统消息
     */
    private String createSystemMessage(String content) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setType("system");
        response.setContent(content);
        response.setCreatedAt(LocalDateTime.now());
        return JSON.toJSONString(response);
    }

    /**
     * 聊天消息DTO
     */
    public static class ChatMessageDTO {
        private String type; // chat, heartbeat, read
        private Long receiverId;
        private Long houseId;
        private String messageType; // text, image, system
        private String content;
        private Long senderId; // 用于已读消息

        // getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        public Long getHouseId() { return houseId; }
        public void setHouseId(Long houseId) { this.houseId = houseId; }
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
    }

    /**
     * 聊天消息响应
     */
    public static class ChatMessageResponse {
        private String type; // message, sent, system
        private Long messageId;
        private String sessionId;
        private Long senderId;
        private Long receiverId;
        private Long houseId;
        private String messageType;
        private String content;
        private LocalDateTime createdAt;

        // getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        public Long getHouseId() { return houseId; }
        public void setHouseId(Long houseId) { this.houseId = houseId; }
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
