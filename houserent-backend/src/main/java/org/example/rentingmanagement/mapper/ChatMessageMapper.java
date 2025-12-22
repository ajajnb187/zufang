package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息数据访问层
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询会话历史消息（按sessionId）
     */
    @Select("SELECT * FROM chat_messages WHERE session_id = #{sessionId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMessage> findSessionMessages(String sessionId, int limit, int offset);

    /**
     * 查询两个用户之间的聊天记录（不依赖sessionId格式）
     */
    @Select("SELECT * FROM chat_messages WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) OR (sender_id = #{userId2} AND receiver_id = #{userId1}) ORDER BY created_at ASC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMessage> findMessagesBetweenUsers(Long userId1, Long userId2, int limit, int offset);

    /**
     * 获取用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE receiver_id = #{userId} AND is_read = 0")
    int getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE chat_messages SET is_read = 1, read_time = NOW() WHERE receiver_id = #{userId} AND sender_id = #{senderId}")
    void markMessagesAsRead(Long userId, Long senderId);

    /**
     * 获取用户最近聊天列表（子查询解决GROUP BY问题）
     */
    @Select("SELECT m.message_id, m.session_id, m.sender_id, m.receiver_id, m.content, m.message_type, m.is_read, m.created_at " +
            "FROM chat_messages m " +
            "INNER JOIN (SELECT session_id, MAX(created_at) as max_time FROM chat_messages " +
            "WHERE sender_id = #{userId} OR receiver_id = #{userId} GROUP BY session_id) latest " +
            "ON m.session_id = latest.session_id AND m.created_at = latest.max_time " +
            "ORDER BY m.created_at DESC")
    List<ChatMessage> findRecentChats(Long userId);
}
