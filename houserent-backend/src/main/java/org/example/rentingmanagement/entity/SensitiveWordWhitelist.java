package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 敏感词白名单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sensitive_word_whitelist")
public class SensitiveWordWhitelist {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("word")
    private String word;

    @TableField("reason")
    private String reason;

    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
