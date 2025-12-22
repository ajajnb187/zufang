package org.example.rentingmanagement.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.CommunityVerificationRequest;
import org.example.rentingmanagement.entity.CommunityVerification;
import org.example.rentingmanagement.mapper.CommunityVerificationMapper;
import org.example.rentingmanagement.service.CommunityVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 小区身份认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityVerificationServiceImpl extends ServiceImpl<CommunityVerificationMapper, CommunityVerification> implements CommunityVerificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityVerification submitVerification(CommunityVerificationRequest request, Long userId) {
        try {
            // 检查用户是否已在该小区有进行中的申请
            CommunityVerification existingVerification = baseMapper.findByUserAndCommunity(userId, request.getCommunityId());
            if (existingVerification != null) {
                // 如果有进行中的申请，不允许重复提交
                if ("pending".equals(existingVerification.getFinalStatus())) {
                    throw new BusinessException("您已有进行中的认证申请，请勿重复提交");
                }
                // 如果已通过认证，不允许重复申请
                if ("approved".equals(existingVerification.getFinalStatus())) {
                    throw new BusinessException("您已通过该小区认证");
                }
            }

            // 创建认证申请
            CommunityVerification verification = new CommunityVerification();
            verification.setUserId(userId);
            verification.setCommunityId(request.getCommunityId());
            verification.setProofType(request.getProofType());
            verification.setProofImages(JSON.toJSONString(request.getProofImages()));
            verification.setApplyReason(request.getApplyReason());
            verification.setCommunityAdminStatus("pending");
            verification.setPlatformAdminStatus("pending");
            verification.setFinalStatus("pending");

            this.save(verification);

            log.info("小区认证申请提交成功: userId={}, communityId={}", userId, request.getCommunityId());
            return verification;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交小区认证申请失败: {}", e.getMessage(), e);
            throw new BusinessException("提交认证申请失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean communityAdminAudit(Long verificationId, Long adminId, boolean approved, String opinion) {
        try {
            CommunityVerification verification = this.getById(verificationId);
            if (verification == null) {
                throw new BusinessException("认证申请不存在");
            }

            if (!"pending".equals(verification.getCommunityAdminStatus())) {
                throw new BusinessException("该申请已处理，无法重复审核");
            }

            // 更新小区管理员审核状态
            verification.setCommunityAdminStatus(approved ? "approved" : "rejected");
            verification.setCommunityAdminId(adminId);
            verification.setCommunityAdminOpinion(opinion);
            verification.setCommunityAdminTime(LocalDateTime.now());

            // 如果小区管理员拒绝，直接设置最终状态为拒绝
            if (!approved) {
                verification.setFinalStatus("rejected");
            }

            this.updateById(verification);

            log.info("小区管理员审核完成: verificationId={}, approved={}", verificationId, approved);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("小区管理员审核失败: {}", e.getMessage(), e);
            throw new BusinessException("审核失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean platformAdminAudit(Long verificationId, Long adminId, boolean approved, String opinion) {
        try {
            CommunityVerification verification = this.getById(verificationId);
            if (verification == null) {
                throw new BusinessException("认证申请不存在");
            }

            // 检查小区管理员是否已通过审核
            if (!"approved".equals(verification.getCommunityAdminStatus())) {
                throw new BusinessException("小区管理员尚未通过审核");
            }

            if (!"pending".equals(verification.getPlatformAdminStatus())) {
                throw new BusinessException("该申请已处理，无法重复审核");
            }

            // 更新平台管理员审核状态
            verification.setPlatformAdminStatus(approved ? "approved" : "rejected");
            verification.setPlatformAdminId(adminId);
            verification.setPlatformAdminOpinion(opinion);
            verification.setPlatformAdminTime(LocalDateTime.now());

            // 设置最终状态
            verification.setFinalStatus(approved ? "approved" : "rejected");

            this.updateById(verification);

            log.info("平台管理员审核完成: verificationId={}, approved={}", verificationId, approved);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("平台管理员审核失败: {}", e.getMessage(), e);
            throw new BusinessException("审核失败");
        }
    }

    @Override
    public CommunityVerification getUserVerificationStatus(Long userId, Long communityId) {
        try {
            return baseMapper.findByUserAndCommunity(userId, communityId);
        } catch (Exception e) {
            log.error("获取用户认证状态失败: userId={}, communityId={}", userId, communityId, e);
            throw new BusinessException("获取认证状态失败");
        }
    }

    @Override
    public IPage<CommunityVerification> getCommunityPendingVerifications(Long communityId, Integer pageNum, Integer pageSize) {
        try {
            Page<CommunityVerification> page = new Page<>(pageNum, pageSize);
            return page.setRecords(baseMapper.findPendingByCommunity(communityId));
        } catch (Exception e) {
            log.error("获取小区待审核认证申请失败: communityId={}", communityId, e);
            throw new BusinessException("获取待审核申请失败");
        }
    }

    @Override
    public IPage<CommunityVerification> getPlatformPendingVerifications(Integer pageNum, Integer pageSize) {
        try {
            Page<CommunityVerification> page = new Page<>(pageNum, pageSize);
            return page.setRecords(baseMapper.findPendingForPlatform());
        } catch (Exception e) {
            log.error("获取平台待审核认证申请失败", e);
            throw new BusinessException("获取待审核申请失败");
        }
    }

    @Override
    public IPage<CommunityVerification> getUserVerificationHistory(Long userId, Integer pageNum, Integer pageSize) {
        try {
            Page<CommunityVerification> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<CommunityVerification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommunityVerification::getUserId, userId)
                   .orderByDesc(CommunityVerification::getCreatedAt);
            return this.page(page, wrapper);
        } catch (Exception e) {
            log.error("获取用户认证历史失败: userId={}", userId, e);
            throw new BusinessException("获取认证历史失败");
        }
    }

    @Override
    public boolean isUserVerified(Long userId, Long communityId) {
        try {
            boolean result = baseMapper.isUserVerified(userId, communityId);
            log.info("【认证检查Service】userId={}, communityId={}, result={}", userId, communityId, result);
            return result;
        } catch (Exception e) {
            log.error("检查用户认证状态失败: userId={}, communityId={}", userId, communityId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelVerification(Long verificationId, Long userId) {
        try {
            CommunityVerification verification = this.getById(verificationId);
            if (verification == null) {
                throw new BusinessException("认证申请不存在");
            }

            // 验证申请人
            if (!verification.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权撤销此申请");
            }

            // 检查状态，只有待审核的申请可以撤销
            if (!"pending".equals(verification.getFinalStatus())) {
                throw new BusinessException("该申请已处理，无法撤销");
            }

            // 删除申请记录
            this.removeById(verificationId);

            log.info("认证申请撤销成功: verificationId={}, userId={}", verificationId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("撤销认证申请失败: {}", e.getMessage(), e);
            throw new BusinessException("撤销申请失败");
        }
    }
}
