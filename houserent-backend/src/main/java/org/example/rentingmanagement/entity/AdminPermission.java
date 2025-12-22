package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员权限配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("admin_permissions")
public class AdminPermission {

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    @TableField("admin_id")
    private Long adminId;

    @TableField("max_house_audit")
    private Integer maxHouseAudit;

    @TableField("max_user_audit")
    private Integer maxUserAudit;

    @TableField("max_contract_audit")
    private Integer maxContractAudit;

    @TableField("can_manage_facilities")
    private Boolean canManageFacilities;

    @TableField("can_handle_reports")
    private Boolean canHandleReports;

    @TableField("can_handle_violations")
    private Boolean canHandleViolations;

    @TableField("status")
    private String status;

    @TableField("remarks")
    private String remarks;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}