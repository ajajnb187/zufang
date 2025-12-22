package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.LandlordApplyRequest;
import org.example.rentingmanagement.dto.LandlordAuditRequest;
import org.example.rentingmanagement.entity.LandlordVerification;
import org.example.rentingmanagement.service.LandlordVerificationService;
import org.springframework.web.bind.annotation.*;

/**
 * 房东认证控制器
 */
@RestController
@RequestMapping("/api/landlord-verification")
@RequiredArgsConstructor
public class LandlordVerificationController {
    
    private final LandlordVerificationService verificationService;
    
    /**
     * 提交房东认证申请
     */
    @PostMapping("/apply")
    public Result<Void> applyLandlord(@RequestBody @Valid LandlordApplyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        verificationService.applyLandlord(request, userId);
        return Result.success("申请已提交，请等待审核");
    }
    
    /**
     * 查询认证状态
     */
    @GetMapping("/status")
    public Result<LandlordVerification> getStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        LandlordVerification verification = verificationService.getVerificationStatus(userId);
        return Result.success(verification);
    }
    
    /**
     * 小区管理员审核
     */
    @PostMapping("/community-audit")
    @SaCheckRole("community_admin")
    public Result<Void> communityAudit(@RequestBody @Valid LandlordAuditRequest request) {
        Long auditorId = StpUtil.getLoginIdAsLong();
        verificationService.communityAudit(request, auditorId);
        return Result.success("审核完成");
    }
    
    /**
     * 平台管理员审核
     */
    @PostMapping("/platform-audit")
    @SaCheckRole("platform_admin")
    public Result<Void> platformAudit(@RequestBody @Valid LandlordAuditRequest request) {
        Long auditorId = StpUtil.getLoginIdAsLong();
        verificationService.platformAudit(request, auditorId);
        return Result.success("审核完成");
    }
}
