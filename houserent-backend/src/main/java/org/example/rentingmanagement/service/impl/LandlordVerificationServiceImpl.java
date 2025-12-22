package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.LandlordApplyRequest;
import org.example.rentingmanagement.dto.LandlordAuditRequest;
import org.example.rentingmanagement.entity.LandlordVerification;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.LandlordVerificationMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.LandlordVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 房东认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LandlordVerificationServiceImpl extends ServiceImpl<LandlordVerificationMapper, LandlordVerification> implements LandlordVerificationService {
    
    private final UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyLandlord(LandlordApplyRequest request, Long userId) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 2. 检查用户是否已经是房东
        if (user.getUserType() == 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "您已经是房东，无需重复申请");
        }
        
        // 3. 检查是否有待审核的申请
        QueryWrapper<LandlordVerification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .in("status", "pending", "community_审核中", "platform_审核中");
        LandlordVerification existing = this.getOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "您有正在审核中的申请，请勿重复提交");
        }
        
        // 4. 创建认证申请
        LandlordVerification verification = new LandlordVerification();
        verification.setUserId(userId);
        verification.setRealName(request.getRealName());
        verification.setIdCard(request.getIdCard());
        verification.setIdCardFront(request.getIdCardFront());
        verification.setIdCardBack(request.getIdCardBack());
        verification.setPropertyProof(request.getPropertyProof());
        verification.setPhone(request.getPhone());
        verification.setCommunityId(request.getCommunityId());
        verification.setStatus("pending");
        verification.setApplyTime(LocalDateTime.now());
        
        this.save(verification);
        log.info("用户{}提交房东认证申请", userId);
    }
    
    @Override
    public LandlordVerification getVerificationStatus(Long userId) {
        QueryWrapper<LandlordVerification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("apply_time")
               .last("LIMIT 1");
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void communityAudit(LandlordAuditRequest request, Long auditorId) {
        LandlordVerification verification = this.getById(request.getVerificationId());
        if (verification == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "认证申请不存在");
        }
        
        if (!"pending".equals(verification.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请已被审核");
        }
        
        verification.setCommunityAuditorId(auditorId);
        verification.setCommunityAuditTime(LocalDateTime.now());
        verification.setCommunityAuditOpinion(request.getOpinion());
        
        if (request.getApproved()) {
            // 小区管理员通过，进入平台管理员审核
            verification.setStatus("platform_审核中");
            log.info("小区管理员{}通过房东认证申请{}", auditorId, request.getVerificationId());
        } else {
            // 小区管理员拒绝，直接拒绝
            verification.setStatus("rejected");
            verification.setRejectReason(request.getRejectReason());
            log.info("小区管理员{}拒绝房东认证申请{}", auditorId, request.getVerificationId());
        }
        
        this.updateById(verification);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void platformAudit(LandlordAuditRequest request, Long auditorId) {
        LandlordVerification verification = this.getById(request.getVerificationId());
        if (verification == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "认证申请不存在");
        }
        
        if (!"platform_审核中".equals(verification.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请不在平台审核状态");
        }
        
        verification.setPlatformAuditorId(auditorId);
        verification.setPlatformAuditTime(LocalDateTime.now());
        verification.setPlatformAuditOpinion(request.getOpinion());
        
        if (request.getApproved()) {
            // 平台管理员通过，更新用户类型为房东
            verification.setStatus("approved");
            
            User user = userMapper.selectById(verification.getUserId());
            user.setUserType(4);
            userMapper.updateById(user);
            
            log.info("平台管理员{}通过房东认证申请{}，用户{}成为房东", auditorId, request.getVerificationId(), user.getUserId());
        } else {
            // 平台管理员拒绝
            verification.setStatus("rejected");
            verification.setRejectReason(request.getRejectReason());
            log.info("平台管理员{}拒绝房东认证申请{}", auditorId, request.getVerificationId());
        }
        
        this.updateById(verification);
    }
}
