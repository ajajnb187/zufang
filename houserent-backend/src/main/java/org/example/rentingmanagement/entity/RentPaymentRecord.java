package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租金支付记录实体类
 * 用于线下支付+线上确认模式
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rent_payment_records")
public class RentPaymentRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @TableField("transaction_id")
    private Long transactionId;

    @TableField("contract_id")
    private Long contractId;

    @TableField("landlord_id")
    private Long landlordId;

    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 支付类型：rent-租金,deposit-押金,utility-水电费,other-其他
     */
    @TableField("payment_type")
    private String paymentType;

    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付周期（如2024-01表示2024年1月）
     */
    @TableField("payment_period")
    private String paymentPeriod;

    /**
     * 支付方式（现金/微信/支付宝/银行转账）
     */
    @TableField("payment_method")
    private String paymentMethod;

    @TableField("tenant_confirmed")
    private Boolean tenantConfirmed;

    @TableField("tenant_confirm_time")
    private LocalDateTime tenantConfirmTime;

    @TableField("landlord_confirmed")
    private Boolean landlordConfirmed;

    @TableField("landlord_confirm_time")
    private LocalDateTime landlordConfirmTime;

    /**
     * 状态：pending-待确认,confirmed-已确认,disputed-有争议
     */
    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非数据库字段 - 用于展示
    @TableField(exist = false)
    private String tenantName;

    @TableField(exist = false)
    private String tenantAvatar;

    @TableField(exist = false)
    private String houseTitle;
}
