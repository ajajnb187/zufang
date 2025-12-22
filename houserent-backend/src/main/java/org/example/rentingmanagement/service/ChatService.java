package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.entity.ChatMessage;

import java.util.List;

/**
 * 聊天管理服务接口
 */
public interface ChatService {

    /**
     * 获取聊天历史记录（按sessionId）
     */
    IPage<ChatMessage> getChatHistory(String sessionId, Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取两个用户之间的聊天历史记录（直接按用户ID查询）
     */
    IPage<ChatMessage> getChatHistoryByUsers(Long userId, Long otherUserId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户未读消息数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markMessagesAsRead(Long userId, Long senderId);

    /**
     * 获取最近聊天列表
     */
    List<Object> getRecentChats(Long userId);

    /**
     * 删除聊天会话
     */
    void deleteChatSession(String sessionId, Long userId);

    /**
     * 获取在线用户数量
     */
    int getOnlineUserCount();

    /**
     * 检查用户是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 发送系统通知消息
     */
    void sendSystemMessage(Long userId, String message);

    /**
     * 生成会话ID
     */
    String generateSessionId(Long userId1, Long userId2);
}
