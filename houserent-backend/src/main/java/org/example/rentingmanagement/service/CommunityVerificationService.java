package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.dto.CommunityVerificationRequest;
import org.example.rentingmanagement.entity.CommunityVerification;

/**
 * 小区身份认证服务接口
 */
public interface CommunityVerificationService {

    /**
     * 提交小区认证申请
     */
    CommunityVerification submitVerification(CommunityVerificationRequest request, Long userId);

    /**
     * 小区管理员审核认证申请
     */
    boolean communityAdminAudit(Long verificationId, Long adminId, boolean approved, String opinion);

    /**
     * 平台管理员审核认证申请
     */
    boolean platformAdminAudit(Long verificationId, Long adminId, boolean approved, String opinion);

    /**
     * 获取用户在指定小区的认证状态
     */
    CommunityVerification getUserVerificationStatus(Long userId, Long communityId);

    /**
     * 获取小区待审核的认证申请列表
     */
    IPage<CommunityVerification> getCommunityPendingVerifications(Long communityId, Integer pageNum, Integer pageSize);

    /**
     * 获取平台待审核的认证申请列表
     */
    IPage<CommunityVerification> getPlatformPendingVerifications(Integer pageNum, Integer pageSize);

    /**
     * 获取用户的认证申请历史
     */
    IPage<CommunityVerification> getUserVerificationHistory(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 检查用户是否已在小区认证通过
     */
    boolean isUserVerified(Long userId, Long communityId);

    /**
     * 撤销认证申请（仅限申请人在待审核状态下）
     */
    boolean cancelVerification(Long verificationId, Long userId);
}
