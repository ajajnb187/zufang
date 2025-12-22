package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用评价实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_evaluations")
public class CreditEvaluation {

    @TableId(value = "evaluation_id", type = IdType.AUTO)
    private Long evaluationId;

    @TableField("contract_id")
    private Long contractId;

    @TableField("evaluator_id")
    private Long evaluatorId;

    @TableField("evaluated_id")
    private Long evaluatedId;

    @TableField("star_rating")
    private BigDecimal starRating;

    @TableField("content")
    private String content;

    @TableField("contract_performance")
    private String contractPerformance;

    @TableField("tags")
    private String tags; // JSON数组

    @TableField("is_anonymous")
    private Boolean isAnonymous;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String evaluatorName;

    @TableField(exist = false)
    private String evaluatorAvatar;

    @TableField(exist = false)
    private String evaluatedName;
}
