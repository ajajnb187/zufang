package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_messages")
public class ChatMessage {

    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @TableField("session_id")
    private String sessionId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("house_id")
    private Long houseId;

    @TableField("message_type")
    private String messageType; // text, image, system

    @TableField("content")
    private String content;

    @TableField("filtered_content")
    private String filteredContent;

    @TableField("has_sensitive")
    private Boolean hasSensitive;

    @TableField("sensitive_words")
    private String sensitiveWords;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
