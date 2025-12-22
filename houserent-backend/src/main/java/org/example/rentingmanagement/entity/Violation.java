package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 违规记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("violations")
public class Violation {

    @TableId(value = "violation_id", type = IdType.AUTO)
    private Long violationId;

    @TableField("user_id")
    private Long userId;

    @TableField("message_id")
    private Long messageId;

    @TableField("violation_type")
    private String violationType;

    @TableField("content")
    private String content;

    @TableField("ai_analysis")
    private String aiAnalysis;

    @TableField("severity")
    private String severity;

    @TableField("status")
    private String status;

    @TableField("action_taken")
    private String actionTaken;

    @TableField("handler_id")
    private Long handlerId;

    @TableField("handler_opinion")
    private String handlerOpinion;

    @TableField("ban_until")
    private LocalDateTime banUntil;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}