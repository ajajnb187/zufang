package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("admins")
public class Admin {

    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long adminId;

    @TableField("user_id")
    private Long userId;

    @TableField("admin_type")
    private String adminType; // platform, community

    @TableField("community_id")
    private Long communityId;

    @TableField("permissions")
    private String permissions; // JSON格式

    @TableField("status")
    private String status; // active, inactive

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
