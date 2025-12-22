package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_notifications")
public class SystemNotification {

    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    @TableField("user_id")
    private Long userId;

    @TableField("notification_type")
    private String notificationType;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("related_type")
    private String relatedType;

    @TableField("related_id")
    private Long relatedId;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
