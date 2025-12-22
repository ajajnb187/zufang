package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租赁交易实体类
 * 用于跟踪整个租赁生命周期：签约 → 入住 → 在租 → 完成 → 评价
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rental_transactions")
public class RentalTransaction {

    @TableId(value = "transaction_id", type = IdType.AUTO)
    private Long transactionId;

    @TableField("contract_id")
    private Long contractId;

    @TableField("house_id")
    private Long houseId;

    @TableField("landlord_id")
    private Long landlordId;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("community_id")
    private Long communityId;

    /**
     * 交易状态：
     * pending_sign - 待签署
     * signed - 已签署待入住
     * pending_checkin - 入住确认中
     * living - 在租中
     * pending_complete - 完成确认中
     * completed - 已完成
     * evaluated - 已评价
     * cancelled - 已取消
     */
    @TableField("status")
    private String status;

    // 入住确认相关
    @TableField("landlord_checkin_confirm")
    private Boolean landlordCheckinConfirm;

    @TableField("landlord_checkin_time")
    private LocalDateTime landlordCheckinTime;

    @TableField("tenant_checkin_confirm")
    private Boolean tenantCheckinConfirm;

    @TableField("tenant_checkin_time")
    private LocalDateTime tenantCheckinTime;

    @TableField("checkin_date")
    private LocalDate checkinDate;

    @TableField("checkin_remark")
    private String checkinRemark;

    // 交易完成确认相关
    @TableField("landlord_complete_confirm")
    private Boolean landlordCompleteConfirm;

    @TableField("landlord_complete_time")
    private LocalDateTime landlordCompleteTime;

    @TableField("tenant_complete_confirm")
    private Boolean tenantCompleteConfirm;

    @TableField("tenant_complete_time")
    private LocalDateTime tenantCompleteTime;

    @TableField("checkout_date")
    private LocalDate checkoutDate;

    @TableField("checkout_remark")
    private String checkoutRemark;

    // 评价状态
    @TableField("landlord_evaluated")
    private Boolean landlordEvaluated;

    @TableField("tenant_evaluated")
    private Boolean tenantEvaluated;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String houseTitle;

    @TableField(exist = false)
    private String communityName;

    @TableField(exist = false)
    private String landlordName;

    @TableField(exist = false)
    private String tenantName;

    @TableField(exist = false)
    private String landlordPhone;

    @TableField(exist = false)
    private String tenantPhone;

    // 合同相关信息
    @TableField(exist = false)
    private String contractStatus;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private java.time.LocalDate contractStartDate;

    @TableField(exist = false)
    private java.time.LocalDate contractEndDate;

    @TableField(exist = false)
    private java.math.BigDecimal rentPrice;

    @TableField(exist = false)
    private RentalContract contract;
}
