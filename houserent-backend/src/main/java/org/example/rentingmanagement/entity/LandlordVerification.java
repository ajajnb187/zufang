package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房东认证审核实体类
 */
@Data
@TableName("landlord_verification")
public class LandlordVerification {
    
    @TableId(type = IdType.AUTO)
    private Long verificationId;
    
    private Long userId;
    
    private String realName;
    
    private String idCard;
    
    private String idCardFront;
    
    private String idCardBack;
    
    private String propertyProof;
    
    private String phone;
    
    private Long communityId;
    
    /**
     * 状态：pending-待审核，community_审核中-小区管理员审核中，
     * platform_审核中-平台管理员审核中，approved-已通过，rejected-已拒绝
     */
    private String status;
    
    private String communityAuditOpinion;
    
    private Long communityAuditorId;
    
    private LocalDateTime communityAuditTime;
    
    private String platformAuditOpinion;
    
    private Long platformAuditorId;
    
    private LocalDateTime platformAuditTime;
    
    private String rejectReason;
    
    private LocalDateTime applyTime;
    
    private LocalDateTime updatedAt;
}
