package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.ContractCreateRequest;
import org.example.rentingmanagement.dto.ContractSignRequest;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.service.ContractService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 电子合同管理控制器
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 创建合同草稿
     */
    @PostMapping("/create")
    public Result<RentalContract> createContract(@RequestBody @Validated ContractCreateRequest request) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        RentalContract contract = contractService.createContractDraft(
                request.getHouseId(),
                landlordId,
                request.getTenantId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMonthlyRent(),
                request.getDeposit(),
                request.getPaymentMethod(),
                request.getCustomContent()
        );
        return Result.success("合同创建成功", contract);
    }

    /**
     * 生成合同PDF
     */
    @PostMapping("/{contractId}/generate-pdf")
    public Result<String> generateContractPDF(@PathVariable Long contractId) {
        contractService.generateContractPDF(contractId);
        // 返回下载URL而非本地路径
        String downloadUrl = "/api/contracts/" + contractId + "/download-pdf";
        return Result.success("PDF生成成功", downloadUrl);
    }
    
    /**
     * 下载合同PDF
     */
    @GetMapping("/{contractId}/download-pdf")
    public void downloadContractPDF(@PathVariable Long contractId, HttpServletResponse response) {
        try {
            RentalContract contract = contractService.getContractDetail(contractId);
            if (contract == null || contract.getPdfUrl() == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            java.nio.file.Path pdfPath = java.nio.file.Paths.get(contract.getPdfUrl());
            if (!java.nio.file.Files.exists(pdfPath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"contract_" + contract.getContractNo() + ".pdf\"");
            response.setHeader("Access-Control-Allow-Origin", "*");
            
            java.nio.file.Files.copy(pdfPath, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 房东签名
     */
    @PostMapping("/{contractId}/landlord-sign")
    @SaCheckRole("landlord")
    public Result<Void> landlordSign(@PathVariable Long contractId,
                                     @RequestBody @Validated ContractSignRequest request) {
        contractService.landlordSign(contractId, request.getSignatureData());
        return Result.success("房东签名成功");
    }

    /**
     * 租户签名
     */
    @PostMapping("/{contractId}/tenant-sign")
    @SaCheckRole("tenant")
    public Result<Void> tenantSign(@PathVariable Long contractId,
                                   @RequestBody @Validated ContractSignRequest request) {
        contractService.tenantSign(contractId, request.getSignatureData());
        return Result.success("租户签名成功");
    }

    /**
     * 管理员审核合同
     */
    @PostMapping("/{contractId}/audit")
    @SaCheckRole("admin")
    public Result<Void> auditContract(@PathVariable Long contractId,
                                      @RequestParam boolean approved,
                                      @RequestParam(required = false) String opinion) {
        Long auditorId = StpUtil.getLoginIdAsLong();
        contractService.auditContract(contractId, auditorId, approved, opinion);
        return Result.success("合同审核完成");
    }

    /**
     * 获取我的合同列表
     */
    @GetMapping("/list")
    public Result<?> getMyContracts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(contractService.getUserContracts(userId, status, pageNum, pageSize));
    }

    /**
     * 获取合同详情
     */
    @GetMapping("/{contractId}")
    public Result<RentalContract> getContractDetail(@PathVariable Long contractId) {
        RentalContract contract = contractService.getContractDetail(contractId);
        return Result.success(contract);
    }

    /**
     * 验证合同完整性
     */
    @GetMapping("/{contractId}/verify")
    public Result<Boolean> verifyContractIntegrity(@PathVariable Long contractId) {
        boolean isValid = contractService.verifyContractIntegrity(contractId);
        return Result.success("合同完整性验证结果", isValid);
    }

    /**
     * 合同变更
     */
    @PostMapping("/{contractId}/amend")
    public Result<RentalContract> amendContract(@PathVariable Long contractId,
                                                @RequestParam String amendmentContent) {
        RentalContract amendedContract = contractService.amendContract(contractId, amendmentContent);
        return Result.success("合同变更成功", amendedContract);
    }

    /**
     * 申请合同解约（需管理员审核）
     */
    @PostMapping("/{contractId}/terminate")
    public Result<Void> terminateContract(@PathVariable Long contractId,
                                          @RequestBody Map<String, String> params) {
        String terminationReason = params.get("terminationReason");
        if (terminationReason == null || terminationReason.trim().isEmpty()) {
            return Result.error("解约原因不能为空");
        }
        contractService.terminateContract(contractId, terminationReason);
        return Result.success("解约申请已提交，等待管理员审核");
    }

    /**
     * 拒绝合同（租户拒绝签署）
     */
    @PostMapping("/{contractId}/reject")
    public Result<Void> rejectContract(@PathVariable Long contractId,
                                       @RequestParam(required = false) String reason) {
        Long userId = StpUtil.getLoginIdAsLong();
        contractService.rejectContract(contractId, userId, reason);
        return Result.success("已拒绝该合同");
    }

    /**
     * 生成合同哈希值
     */
    @PostMapping("/{contractId}/generate-hash")
    public Result<String> generateContractHash(@PathVariable Long contractId) {
        String hash = contractService.generateContractHash(contractId);
        return Result.success("哈希值生成成功", hash);
    }
}
