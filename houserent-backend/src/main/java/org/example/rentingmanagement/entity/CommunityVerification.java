package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 小区认证实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_verifications")
public class CommunityVerification {

    @TableId(value = "verification_id", type = IdType.AUTO)
    private Long verificationId;

    @TableField("user_id")
    private Long userId;

    @TableField("community_id")
    private Long communityId;

    @TableField("proof_type")
    private String proofType; // rental_contract, property_fee, ownership_cert, other

    @TableField("proof_images")
    private String proofImages; // JSON数组

    @TableField("apply_reason")
    private String applyReason;

    @TableField("community_admin_status")
    private String communityAdminStatus; // pending, approved, rejected

    @TableField("community_admin_id")
    private Long communityAdminId;

    @TableField("community_admin_opinion")
    private String communityAdminOpinion;

    @TableField("community_admin_time")
    private LocalDateTime communityAdminTime;

    @TableField("platform_admin_status")
    private String platformAdminStatus; // pending, approved, rejected

    @TableField("platform_admin_id")
    private Long platformAdminId;

    @TableField("platform_admin_opinion")
    private String platformAdminOpinion;

    @TableField("platform_admin_time")
    private LocalDateTime platformAdminTime;

    @TableField("final_status")
    private String finalStatus; // pending, approved, rejected

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
