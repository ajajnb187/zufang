package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租赁合同实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rental_contracts")
public class RentalContract {

    @TableId(value = "contract_id", type = IdType.AUTO)
    private Long contractId;

    @TableField("contract_no")
    private String contractNo;

    @TableField("house_id")
    private Long houseId;

    @TableField("landlord_id")
    private Long landlordId;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("community_id")
    private Long communityId;

    @TableField("template_content")
    private String templateContent;

    @TableField("custom_content")
    private String customContent;

    @TableField("rent_price")
    private BigDecimal rentPrice;

    @TableField("deposit")
    private BigDecimal deposit;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("landlord_signature")
    private String landlordSignature; // Base64

    @TableField("landlord_sign_time")
    private LocalDateTime landlordSignTime;

    @TableField("tenant_signature")
    private String tenantSignature; // Base64

    @TableField("tenant_sign_time")
    private LocalDateTime tenantSignTime;

    @TableField("contract_hash")
    private String contractHash; // 防篡改哈希值

    @TableField("audit_status")
    private String auditStatus; // draft, pending, approved, rejected

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("audit_opinion")
    private String auditOpinion;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("contract_status")
    private String contractStatus; // draft, effective, terminated, expired

    @TableField("pdf_url")
    private String pdfUrl;

    @TableField("termination_reason")
    private String terminationReason; // 解约原因

    @TableField("termination_requested_by")
    private Long terminationRequestedBy; // 申请解约的用户ID

    @TableField("termination_request_time")
    private LocalDateTime terminationRequestTime; // 申请解约时间

    @TableField("termination_status")
    private String terminationStatus; // none, pending, approved, rejected

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ========== 非数据库字段，用于前端展示 ==========

    /**
     * 房源标题
     */
    @TableField(exist = false)
    private String houseTitle;

    /**
     * 房源地址
     */
    @TableField(exist = false)
    private String houseAddress;

    /**
     * 房源图片
     */
    @TableField(exist = false)
    private String houseImage;

    /**
     * 房源类型
     */
    @TableField(exist = false)
    private String houseType;

    /**
     * 房源面积
     */
    @TableField(exist = false)
    private java.math.BigDecimal houseArea;

    /**
     * 房东姓名
     */
    @TableField(exist = false)
    private String landlordName;

    /**
     * 房东电话
     */
    @TableField(exist = false)
    private String landlordPhone;

    /**
     * 房东头像
     */
    @TableField(exist = false)
    private String landlordAvatar;

    /**
     * 租客姓名
     */
    @TableField(exist = false)
    private String tenantName;

    /**
     * 租客电话
     */
    @TableField(exist = false)
    private String tenantPhone;

    /**
     * 租客头像
     */
    @TableField(exist = false)
    private String tenantAvatar;

    /**
     * 小区名称
     */
    @TableField(exist = false)
    private String communityName;
}
