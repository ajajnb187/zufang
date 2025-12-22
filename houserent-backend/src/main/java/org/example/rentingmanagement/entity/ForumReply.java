package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 论坛回复实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("forum_replies")
public class ForumReply {

    @TableId(value = "reply_id", type = IdType.AUTO)
    private Long replyId;

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_reply_id")
    private Long parentReplyId;

    @TableField("content")
    private String content;

    @TableField("images")
    private String images; // JSON数组

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
