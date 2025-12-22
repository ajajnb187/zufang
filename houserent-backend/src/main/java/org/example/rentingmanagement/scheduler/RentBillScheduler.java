package org.example.rentingmanagement.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.RentalTransaction;
import org.example.rentingmanagement.entity.RentPaymentRecord;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.RentalTransactionMapper;
import org.example.rentingmanagement.mapper.RentPaymentRecordMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 租金账单定时任务
 * 每月自动为在租状态的交易生成租金账单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RentBillScheduler {

    private final RentalTransactionMapper transactionMapper;
    private final RentalContractMapper contractMapper;
    private final RentPaymentRecordMapper paymentRecordMapper;

    /**
     * 每月1日凌晨2点自动生成当月租金账单
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void generateMonthlyRentBills() {
        log.info("开始执行月度租金账单生成任务...");
        
        try {
            // 获取所有在租中的交易
            LambdaQueryWrapper<RentalTransaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RentalTransaction::getStatus, "living");
            List<RentalTransaction> livingTransactions = transactionMapper.selectList(wrapper);
            
            LocalDate today = LocalDate.now();
            String currentPeriod = today.getYear() + "年" + today.getMonthValue() + "月";
            
            int successCount = 0;
            int skipCount = 0;
            
            for (RentalTransaction transaction : livingTransactions) {
                try {
                    // 检查本月是否已有账单
                    boolean exists = checkBillExists(transaction.getTransactionId(), currentPeriod);
                    if (exists) {
                        skipCount++;
                        continue;
                    }
                    
                    // 获取合同信息
                    RentalContract contract = contractMapper.selectById(transaction.getContractId());
                    if (contract == null || contract.getRentPrice() == null) {
                        log.warn("合同不存在或租金为空: transactionId={}", transaction.getTransactionId());
                        continue;
                    }
                    
                    // 检查合同是否在有效期内
                    if (contract.getEndDate() != null && contract.getEndDate().isBefore(today)) {
                        log.info("合同已到期，跳过: contractId={}", contract.getContractId());
                        continue;
                    }
                    
                    // 创建租金账单
                    createRentBill(transaction, contract, currentPeriod);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("生成账单失败: transactionId={}", transaction.getTransactionId(), e);
                }
            }
            
            log.info("月度租金账单生成完成: 成功={}, 跳过={}, 总计={}", 
                successCount, skipCount, livingTransactions.size());
                
        } catch (Exception e) {
            log.error("月度租金账单生成任务执行失败", e);
        }
    }
    
    /**
     * 检查指定账期是否已有账单
     */
    private boolean checkBillExists(Long transactionId, String period) {
        LambdaQueryWrapper<RentPaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentPaymentRecord::getTransactionId, transactionId)
               .eq(RentPaymentRecord::getPaymentPeriod, period)
               .eq(RentPaymentRecord::getPaymentType, "rent");
        return paymentRecordMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 创建租金账单
     */
    private void createRentBill(RentalTransaction transaction, RentalContract contract, String period) {
        RentPaymentRecord record = new RentPaymentRecord();
        record.setTransactionId(transaction.getTransactionId());
        record.setContractId(contract.getContractId());
        record.setLandlordId(transaction.getLandlordId());
        record.setTenantId(transaction.getTenantId());
        record.setAmount(contract.getRentPrice());
        record.setPaymentType("rent");
        record.setPaymentPeriod(period);
        record.setStatus("pending");
        record.setRemark("系统自动生成的月租账单");
        
        paymentRecordMapper.insert(record);
        log.info("创建租金账单: transactionId={}, period={}, amount={}", 
            transaction.getTransactionId(), period, contract.getRentPrice());
    }
    
    /**
     * 手动触发生成账单（用于补单或测试）
     */
    public void generateBillsForTransaction(Long transactionId) {
        RentalTransaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null || !"living".equals(transaction.getStatus())) {
            log.warn("交易不存在或不在租中: transactionId={}", transactionId);
            return;
        }
        
        RentalContract contract = contractMapper.selectById(transaction.getContractId());
        if (contract == null) {
            log.warn("合同不存在: transactionId={}", transactionId);
            return;
        }
        
        // 从入住日期开始，生成到当前月份的所有账单
        LocalDate startDate = transaction.getCheckinDate() != null 
            ? transaction.getCheckinDate() 
            : contract.getStartDate();
        LocalDate endDate = LocalDate.now();
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        
        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            String period = current.getYear() + "年" + current.getMonthValue() + "月";
            
            if (!checkBillExists(transactionId, period)) {
                createRentBill(transaction, contract, period);
            }
            
            current = current.plusMonths(1);
        }
    }
}
