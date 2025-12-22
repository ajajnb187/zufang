package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配套设施反馈实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("facility_feedbacks")
public class FacilityFeedback {

    @TableId(value = "feedback_id", type = IdType.AUTO)
    private Long feedbackId;

    @TableField("community_id")
    private Long communityId;

    @TableField("facility_id")
    private Long facilityId;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status; // pending, processed, rejected

    @TableField("handler_id")
    private Long handlerId;

    @TableField("handler_reply")
    private String handlerReply;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
