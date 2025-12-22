package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.RentPaymentRecord;
import org.example.rentingmanagement.entity.RentalTransaction;
import org.example.rentingmanagement.mapper.RentPaymentRecordMapper;
import org.example.rentingmanagement.mapper.RentalTransactionMapper;
import org.springframework.web.bind.annotation.*;

import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.scheduler.RentBillScheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租金支付记录控制器
 * 支持线下支付+线上确认模式
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class RentPaymentController {

    private final RentPaymentRecordMapper paymentRecordMapper;
    private final RentalTransactionMapper transactionMapper;
    private final RentalContractMapper contractMapper;
    private final RentBillScheduler rentBillScheduler;

    /**
     * 创建支付记录（房东或租客都可发起）
     */
    @PostMapping("/create")
    public Result<RentPaymentRecord> createPaymentRecord(@RequestBody RentPaymentRecord record) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 验证交易存在且用户是交易参与方
        RentalTransaction transaction = transactionMapper.selectById(record.getTransactionId());
        if (transaction == null) {
            return Result.error("交易不存在");
        }
        
        if (!transaction.getLandlordId().equals(userId) && !transaction.getTenantId().equals(userId)) {
            return Result.error("无权操作此交易");
        }
        
        // 设置基本信息
        record.setLandlordId(transaction.getLandlordId());
        record.setTenantId(transaction.getTenantId());
        record.setContractId(transaction.getContractId());
        record.setStatus("pending");
        
        // 如果是租客发起，自动确认租客已支付
        if (transaction.getTenantId().equals(userId)) {
            record.setTenantConfirmed(true);
            record.setTenantConfirmTime(LocalDateTime.now());
        }
        // 如果是房东发起（记录收款），自动确认房东已收款
        if (transaction.getLandlordId().equals(userId)) {
            record.setLandlordConfirmed(true);
            record.setLandlordConfirmTime(LocalDateTime.now());
        }
        
        paymentRecordMapper.insert(record);
        log.info("创建支付记录: recordId={}, transactionId={}, amount={}", 
                record.getRecordId(), record.getTransactionId(), record.getAmount());
        
        return Result.success("支付记录创建成功", record);
    }

    /**
     * 确认支付/收款
     */
    @PostMapping("/{recordId}/confirm")
    public Result<Void> confirmPayment(@PathVariable Long recordId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        RentPaymentRecord record = paymentRecordMapper.selectById(recordId);
        if (record == null) {
            return Result.error("记录不存在");
        }
        
        // 租客确认已支付
        if (record.getTenantId().equals(userId) && !record.getTenantConfirmed()) {
            record.setTenantConfirmed(true);
            record.setTenantConfirmTime(LocalDateTime.now());
        }
        // 房东确认已收款
        else if (record.getLandlordId().equals(userId) && !record.getLandlordConfirmed()) {
            record.setLandlordConfirmed(true);
            record.setLandlordConfirmTime(LocalDateTime.now());
        }
        else {
            return Result.error("您已确认或无权确认");
        }
        
        // 双方都确认后，状态变为已确认
        if (record.getTenantConfirmed() && record.getLandlordConfirmed()) {
            record.setStatus("confirmed");
        }
        
        paymentRecordMapper.updateById(record);
        log.info("确认支付: recordId={}, userId={}", recordId, userId);
        
        return Result.success("确认成功");
    }

    /**
     * 获取房东的收款记录
     */
    @GetMapping("/landlord/records")
    public Result<List<RentPaymentRecord>> getLandlordRecords() {
        Long landlordId = StpUtil.getLoginIdAsLong();
        List<RentPaymentRecord> records = paymentRecordMapper.findByLandlordId(landlordId);
        return Result.success(records);
    }

    /**
     * 获取租客的支付记录
     */
    @GetMapping("/tenant/records")
    public Result<List<RentPaymentRecord>> getTenantRecords() {
        Long tenantId = StpUtil.getLoginIdAsLong();
        List<RentPaymentRecord> records = paymentRecordMapper.findByTenantId(tenantId);
        return Result.success(records);
    }

    /**
     * 获取房东收益统计（合同驱动，自动计算）
     */
    @GetMapping("/landlord/statistics")
    public Result<Map<String, Object>> getLandlordStatistics(
            @RequestParam(required = false) String period) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 已确认收益（已确认的支付记录）
        BigDecimal confirmedRevenue = paymentRecordMapper.sumTotalByLandlord(landlordId);
        stats.put("totalRevenue", confirmedRevenue != null ? confirmedRevenue : BigDecimal.ZERO);
        
        // 2. 本月预期收益（基于在租合同自动计算）
        BigDecimal expectedMonthRevenue = calculateExpectedMonthRevenue(landlordId);
        stats.put("expectedMonthRevenue", expectedMonthRevenue);
        
        // 3. 本月已确认收益
        LocalDate now = LocalDate.now();
        String currentPeriod = now.getYear() + "年" + now.getMonthValue() + "月";
        LambdaQueryWrapper<RentPaymentRecord> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(RentPaymentRecord::getLandlordId, landlordId)
                   .eq(RentPaymentRecord::getPaymentPeriod, currentPeriod)
                   .eq(RentPaymentRecord::getStatus, "confirmed");
        List<RentPaymentRecord> monthRecords = paymentRecordMapper.selectList(monthWrapper);
        BigDecimal monthRevenue = monthRecords.stream()
                .map(RentPaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthRevenue", monthRevenue);
        
        // 4. 待确认金额
        LambdaQueryWrapper<RentPaymentRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(RentPaymentRecord::getLandlordId, landlordId)
                     .eq(RentPaymentRecord::getStatus, "pending");
        List<RentPaymentRecord> pendingRecords = paymentRecordMapper.selectList(pendingWrapper);
        BigDecimal pendingAmount = pendingRecords.stream()
                .map(RentPaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingAmount", pendingAmount);
        
        // 5. 在租房源数量
        LambdaQueryWrapper<RentalTransaction> livingWrapper = new LambdaQueryWrapper<>();
        livingWrapper.eq(RentalTransaction::getLandlordId, landlordId)
                    .eq(RentalTransaction::getStatus, "living");
        long livingCount = transactionMapper.selectCount(livingWrapper);
        stats.put("livingCount", livingCount);
        
        // 6. 按月统计
        List<Map<String, Object>> monthlyStats = paymentRecordMapper.monthlyStatsByLandlord(landlordId);
        stats.put("monthlyStats", monthlyStats);
        
        return Result.success(stats);
    }
    
    /**
     * 计算本月预期收益（基于所有在租合同）
     */
    private BigDecimal calculateExpectedMonthRevenue(Long landlordId) {
        LambdaQueryWrapper<RentalTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalTransaction::getLandlordId, landlordId)
               .eq(RentalTransaction::getStatus, "living");
        List<RentalTransaction> livingTransactions = transactionMapper.selectList(wrapper);
        
        BigDecimal total = BigDecimal.ZERO;
        for (RentalTransaction tx : livingTransactions) {
            RentalContract contract = contractMapper.selectById(tx.getContractId());
            if (contract != null && contract.getRentPrice() != null) {
                total = total.add(contract.getRentPrice());
            }
        }
        return total;
    }
    
    /**
     * 为指定交易生成历史账单（入住确认时调用）
     */
    @PostMapping("/generate-bills/{transactionId}")
    public Result<Void> generateBillsForTransaction(@PathVariable Long transactionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        RentalTransaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            return Result.error("交易不存在");
        }
        if (!transaction.getLandlordId().equals(userId)) {
            return Result.error("无权操作");
        }
        
        rentBillScheduler.generateBillsForTransaction(transactionId);
        return Result.success("账单生成成功");
    }

    /**
     * 获取交易的支付记录
     */
    @GetMapping("/transaction/{transactionId}")
    public Result<List<RentPaymentRecord>> getTransactionRecords(@PathVariable Long transactionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 验证用户是交易参与方
        RentalTransaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            return Result.error("交易不存在");
        }
        if (!transaction.getLandlordId().equals(userId) && !transaction.getTenantId().equals(userId)) {
            return Result.error("无权查看");
        }
        
        List<RentPaymentRecord> records = paymentRecordMapper.findByTransactionId(transactionId);
        return Result.success(records);
    }

    /**
     * 发起争议
     */
    @PostMapping("/{recordId}/dispute")
    public Result<Void> disputePayment(@PathVariable Long recordId,
                                       @RequestParam String reason) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        RentPaymentRecord record = paymentRecordMapper.selectById(recordId);
        if (record == null) {
            return Result.error("记录不存在");
        }
        
        if (!record.getTenantId().equals(userId) && !record.getLandlordId().equals(userId)) {
            return Result.error("无权操作");
        }
        
        record.setStatus("disputed");
        record.setRemark("争议原因: " + reason);
        paymentRecordMapper.updateById(record);
        
        log.info("发起支付争议: recordId={}, userId={}, reason={}", recordId, userId, reason);
        return Result.success("已提交争议");
    }
}
