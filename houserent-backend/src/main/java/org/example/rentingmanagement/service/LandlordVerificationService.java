package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.rentingmanagement.dto.LandlordApplyRequest;
import org.example.rentingmanagement.dto.LandlordAuditRequest;
import org.example.rentingmanagement.entity.LandlordVerification;

/**
 * 房东认证服务接口
 */
public interface LandlordVerificationService extends IService<LandlordVerification> {
    
    /**
     * 提交房东认证申请
     */
    void applyLandlord(LandlordApplyRequest request, Long userId);
    
    /**
     * 查询用户的认证状态
     */
    LandlordVerification getVerificationStatus(Long userId);
    
    /**
     * 小区管理员审核
     */
    void communityAudit(LandlordAuditRequest request, Long auditorId);
    
    /**
     * 平台管理员审核
     */
    void platformAudit(LandlordAuditRequest request, Long auditorId);
}
