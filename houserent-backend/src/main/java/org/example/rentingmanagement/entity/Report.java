package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 举报记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("reports")
public class Report {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("report_type")
    private String reportType; // house, user, facility, other

    @TableField("target_id")
    private Long targetId;

    @TableField("reason_type")
    private String reasonType;

    @TableField("reason_detail")
    private String reasonDetail;

    @TableField("evidence_images")
    private String evidenceImages; // JSON数组

    @TableField("status")
    private String status; // pending, processing, handled, rejected

    @TableField("handler_id")
    private Long handlerId;

    @TableField("handle_result")
    private String handleResult;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
