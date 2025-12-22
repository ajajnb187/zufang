package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.CommunityVerificationRequest;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.CommunityVerification;
import org.example.rentingmanagement.service.CommunityService;
import org.example.rentingmanagement.service.CommunityVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小区身份认证控制器
 */
@RestController
@RequestMapping("/api/community-verification")
@RequiredArgsConstructor
public class CommunityVerificationController {

    private final CommunityVerificationService communityVerificationService;
    private final CommunityService communityService;

    /**
     * 提交小区认证申请
     */
    @PostMapping("/submit")
    public Result<CommunityVerification> submitVerification(@RequestBody @Valid CommunityVerificationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityVerification verification = communityVerificationService.submitVerification(request, userId);
        return Result.success("认证申请提交成功", verification);
    }

    /**
     * 获取用户在指定小区的认证状态（包含小区名称）
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getUserVerificationStatus(@RequestParam Long communityId) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityVerification verification = communityVerificationService.getUserVerificationStatus(userId, communityId);
        if (verification == null) {
            return Result.success(null);
        }
        // 构建返回数据，包含小区名称
        Map<String, Object> result = new HashMap<>();
        result.put("verificationId", verification.getVerificationId());
        result.put("userId", verification.getUserId());
        result.put("communityId", verification.getCommunityId());
        result.put("proofType", verification.getProofType());
        result.put("proofImages", verification.getProofImages());
        result.put("applyReason", verification.getApplyReason());
        result.put("communityAdminStatus", verification.getCommunityAdminStatus());
        result.put("communityAdminOpinion", verification.getCommunityAdminOpinion());
        result.put("platformAdminStatus", verification.getPlatformAdminStatus());
        result.put("platformAdminOpinion", verification.getPlatformAdminOpinion());
        result.put("finalStatus", verification.getFinalStatus());
        result.put("createdAt", verification.getCreatedAt());
        // 获取小区名称
        Community community = communityService.getCommunityDetail(communityId);
        if (community != null) {
            result.put("communityName", community.getCommunityName());
        }
        return Result.success(result);
    }

    /**
     * 获取用户的认证申请历史（包含小区名称）
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getUserVerificationHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<CommunityVerification> history = communityVerificationService.getUserVerificationHistory(userId, pageNum, pageSize);
        
        // 为每条记录添加小区名称
        List<Map<String, Object>> recordsWithCommunityName = new ArrayList<>();
        for (CommunityVerification v : history.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("verificationId", v.getVerificationId());
            record.put("userId", v.getUserId());
            record.put("communityId", v.getCommunityId());
            record.put("proofType", v.getProofType());
            record.put("proofImages", v.getProofImages());
            record.put("applyReason", v.getApplyReason());
            record.put("communityAdminStatus", v.getCommunityAdminStatus());
            record.put("communityAdminOpinion", v.getCommunityAdminOpinion());
            record.put("platformAdminStatus", v.getPlatformAdminStatus());
            record.put("platformAdminOpinion", v.getPlatformAdminOpinion());
            record.put("finalStatus", v.getFinalStatus());
            record.put("createdAt", v.getCreatedAt());
            // 获取小区名称
            Community community = communityService.getCommunityDetail(v.getCommunityId());
            if (community != null) {
                record.put("communityName", community.getCommunityName());
            }
            recordsWithCommunityName.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", recordsWithCommunityName);
        result.put("total", history.getTotal());
        result.put("current", history.getCurrent());
        result.put("size", history.getSize());
        return Result.success(result);
    }

    /**
     * 撤销认证申请
     */
    @DeleteMapping("/{verificationId}")
    public Result<Void> cancelVerification(@PathVariable Long verificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        communityVerificationService.cancelVerification(verificationId, userId);
        return Result.success("申请撤销成功");
    }

    /**
     * 小区管理员审核认证申请
     */
    @PostMapping("/{verificationId}/community-audit")
    @SaCheckRole("admin")
    public Result<Void> communityAdminAudit(@PathVariable Long verificationId,
                                           @RequestParam boolean approved,
                                           @RequestParam(required = false) String opinion) {
        Long adminId = StpUtil.getLoginIdAsLong();
        communityVerificationService.communityAdminAudit(verificationId, adminId, approved, opinion);
        return Result.success("审核完成");
    }

    /**
     * 平台管理员审核认证申请
     */
    @PostMapping("/{verificationId}/platform-audit")
    @SaCheckRole("admin")
    public Result<Void> platformAdminAudit(@PathVariable Long verificationId,
                                         @RequestParam boolean approved,
                                         @RequestParam(required = false) String opinion) {
        Long adminId = StpUtil.getLoginIdAsLong();
        communityVerificationService.platformAdminAudit(verificationId, adminId, approved, opinion);
        return Result.success("审核完成");
    }

    /**
     * 获取小区待审核的认证申请列表
     */
    @GetMapping("/community/{communityId}/pending")
    @SaCheckRole("admin")
    public Result<IPage<CommunityVerification>> getCommunityPendingVerifications(
            @PathVariable Long communityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<CommunityVerification> verifications = communityVerificationService.getCommunityPendingVerifications(communityId, pageNum, pageSize);
        return Result.success(verifications);
    }

    /**
     * 获取平台待审核的认证申请列表
     */
    @GetMapping("/platform/pending")
    @SaCheckRole("admin")
    public Result<IPage<CommunityVerification>> getPlatformPendingVerifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<CommunityVerification> verifications = communityVerificationService.getPlatformPendingVerifications(pageNum, pageSize);
        return Result.success(verifications);
    }

    /**
     * 检查用户是否已在小区认证通过
     */
    @GetMapping("/check")
    public Result<Boolean> checkUserVerified(@RequestParam Long communityId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean verified = communityVerificationService.isUserVerified(userId, communityId);
        System.out.println("【认证检查】userId=" + userId + ", communityId=" + communityId + ", verified=" + verified);
        return Result.success(verified);
    }
}
