package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 敏感词实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sensitive_words")
public class SensitiveWord {

    @TableId(value = "word_id", type = IdType.AUTO)
    private Long wordId;

    @TableField("word")
    private String word;

    @TableField("category")
    private String category;

    @TableField("level")
    private Integer level;

    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
