package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.RentalTransaction;
import org.example.rentingmanagement.service.RentalTransactionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 租赁交易控制器
 * 处理入住确认、交易完成、评价等流程
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class RentalTransactionController {

    private final RentalTransactionService rentalTransactionService;

    /**
     * 获取我的交易列表
     */
    @GetMapping("/my")
    public Result<List<RentalTransaction>> getMyTransactions(
            @RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<RentalTransaction> transactions = rentalTransactionService.getUserTransactions(userId, status);
        return Result.success(transactions);
    }

    /**
     * 获取在租中的交易
     */
    @GetMapping("/living")
    public Result<List<RentalTransaction>> getLivingTransactions() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<RentalTransaction> transactions = rentalTransactionService.getUserLivingTransactions(userId);
        return Result.success(transactions);
    }

    /**
     * 获取交易详情
     */
    @GetMapping("/{transactionId}")
    public Result<RentalTransaction> getTransactionDetail(@PathVariable Long transactionId) {
        RentalTransaction transaction = rentalTransactionService.getTransactionDetail(transactionId);
        if (transaction == null) {
            return Result.error("交易记录不存在");
        }
        return Result.success(transaction);
    }

    /**
     * 确认入住
     */
    @PostMapping("/{transactionId}/checkin")
    public Result<Void> confirmCheckin(
            @PathVariable Long transactionId,
            @RequestBody(required = false) Map<String, Object> data) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            LocalDate checkinDate = null;
            String remark = null;
            
            if (data != null) {
                if (data.get("checkinDate") != null) {
                    checkinDate = LocalDate.parse(data.get("checkinDate").toString());
                }
                remark = (String) data.get("remark");
            }
            
            rentalTransactionService.confirmCheckin(transactionId, userId, checkinDate, remark);
            return Result.success("入住确认成功");
        } catch (Exception e) {
            log.error("确认入住失败: transactionId={}", transactionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认交易完成
     */
    @PostMapping("/{transactionId}/complete")
    public Result<Void> confirmComplete(
            @PathVariable Long transactionId,
            @RequestBody(required = false) Map<String, Object> data) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            LocalDate checkoutDate = null;
            String remark = null;
            
            if (data != null) {
                if (data.get("checkoutDate") != null) {
                    checkoutDate = LocalDate.parse(data.get("checkoutDate").toString());
                }
                remark = (String) data.get("remark");
            }
            
            rentalTransactionService.confirmComplete(transactionId, userId, checkoutDate, remark);
            return Result.success("确认成功");
        } catch (Exception e) {
            log.error("确认交易完成失败: transactionId={}", transactionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消交易
     */
    @PostMapping("/{transactionId}/cancel")
    public Result<Void> cancelTransaction(
            @PathVariable Long transactionId,
            @RequestParam String reason) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            rentalTransactionService.cancelTransaction(transactionId, userId, reason);
            return Result.success("交易已取消");
        } catch (Exception e) {
            log.error("取消交易失败: transactionId={}", transactionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据合同ID获取交易
     */
    @GetMapping("/contract/{contractId}")
    public Result<RentalTransaction> getByContractId(@PathVariable Long contractId) {
        RentalTransaction transaction = rentalTransactionService.getByContractId(contractId);
        return Result.success(transaction);
    }
}
