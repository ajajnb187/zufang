package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("forum_posts")
public class ForumPost {

    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @TableField("community_id")
    private Long communityId;

    @TableField("post_type")
    private String postType; // rent_need, share_info, discussion

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("budget_min")
    private BigDecimal budgetMin;

    @TableField("budget_max")
    private BigDecimal budgetMax;

    @TableField("expected_area")
    private String expectedArea;

    @TableField("expected_type")
    private String expectedType;

    @TableField("images")
    private String images; // JSON数组

    @TableField("is_urgent")
    private Boolean isUrgent;

    @TableField("status")
    private String status; // active, closed, deleted

    @TableField("view_count")
    private Integer viewCount;

    @TableField("reply_count")
    private Integer replyCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
