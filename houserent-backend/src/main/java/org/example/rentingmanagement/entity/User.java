package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("openid")
    private String openid;

    @TableField("password")
    private String password;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("phone")
    private String phone;

    @TableField("user_type")
    private Integer userType; // 3-租户, 4-房东

    @TableField("credit_score")
    private BigDecimal creditScore;

    @TableField("status")
    private String status; // active, banned, deleted

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
