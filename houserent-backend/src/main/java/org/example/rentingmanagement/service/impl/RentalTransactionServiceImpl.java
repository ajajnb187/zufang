package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.RentalTransaction;
import org.example.rentingmanagement.entity.RentPaymentRecord;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.RentalTransactionMapper;
import org.example.rentingmanagement.mapper.RentPaymentRecordMapper;
import org.example.rentingmanagement.service.RentalTransactionService;
import org.example.rentingmanagement.service.SystemNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 租赁交易服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalTransactionServiceImpl extends ServiceImpl<RentalTransactionMapper, RentalTransaction> 
        implements RentalTransactionService {

    private final RentalTransactionMapper rentalTransactionMapper;
    private final RentalContractMapper rentalContractMapper;
    private final RentPaymentRecordMapper rentPaymentRecordMapper;
    private final SystemNotificationService systemNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentalTransaction createFromContract(Long contractId) {
        // 检查合同是否存在
        RentalContract contract = rentalContractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        // 检查是否已有交易记录
        RentalTransaction existing = rentalTransactionMapper.findByContractId(contractId);
        if (existing != null) {
            return existing;
        }

        // 创建交易记录
        RentalTransaction transaction = new RentalTransaction();
        transaction.setContractId(contractId);
        transaction.setHouseId(contract.getHouseId());
        transaction.setLandlordId(contract.getLandlordId());
        transaction.setTenantId(contract.getTenantId());
        transaction.setCommunityId(contract.getCommunityId());
        transaction.setStatus("pending_sign");
        transaction.setLandlordCheckinConfirm(false);
        transaction.setTenantCheckinConfirm(false);
        transaction.setLandlordCompleteConfirm(false);
        transaction.setTenantCompleteConfirm(false);
        transaction.setLandlordEvaluated(false);
        transaction.setTenantEvaluated(false);

        rentalTransactionMapper.insert(transaction);
        log.info("创建租赁交易记录: transactionId={}, contractId={}", transaction.getTransactionId(), contractId);

        return transaction;
    }

    @Override
    public List<RentalTransaction> getUserTransactions(Long userId, String status) {
        return rentalTransactionMapper.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<RentalTransaction> getUserLivingTransactions(Long userId) {
        return rentalTransactionMapper.findLivingByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCheckin(Long transactionId, Long userId, LocalDate checkinDate, String remark) {
        RentalTransaction transaction = getById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 检查合同状态 - 只有合同生效后才能确认入住
        RentalContract contract = rentalContractMapper.selectById(transaction.getContractId());
        if (contract == null) {
            throw new BusinessException("关联合同不存在");
        }
        
        if (!"effective".equals(contract.getContractStatus())) {
            String statusMsg = getContractStatusMessage(contract.getContractStatus());
            throw new BusinessException("合同" + statusMsg + "，无法确认入住。请等待合同生效后再操作。");
        }

        // 验证用户身份并更新确认状态
        boolean isLandlord = userId.equals(transaction.getLandlordId());
        boolean isTenant = userId.equals(transaction.getTenantId());

        if (!isLandlord && !isTenant) {
            throw new BusinessException("您无权操作此交易");
        }

        if (isLandlord) {
            transaction.setLandlordCheckinConfirm(true);
            transaction.setLandlordCheckinTime(LocalDateTime.now());
        } else {
            transaction.setTenantCheckinConfirm(true);
            transaction.setTenantCheckinTime(LocalDateTime.now());
        }

        if (checkinDate != null) {
            transaction.setCheckinDate(checkinDate);
        }
        if (remark != null && !remark.isEmpty()) {
            transaction.setCheckinRemark(remark);
        }

        // 检查双方是否都已确认
        if (Boolean.TRUE.equals(transaction.getLandlordCheckinConfirm()) 
                && Boolean.TRUE.equals(transaction.getTenantCheckinConfirm())) {
            transaction.setStatus("living");
            log.info("双方确认入住，交易状态变更为在租中: transactionId={}", transactionId);
            
            // 自动生成从入住日期到当前的所有租金账单（已确认状态）
            generateAllRentBills(transaction, contract);
            
            // 发送通知
            sendCheckinNotification(transaction);
        } else {
            transaction.setStatus("pending_checkin");
        }

        updateById(transaction);
    }
    
    /**
     * 获取合同状态的中文描述
     */
    private String getContractStatusMessage(String status) {
        if (status == null) return "状态未知";
        switch (status) {
            case "draft": return "尚未签署";
            case "signed": return "待审核中";
            case "effective": return "已生效";
            case "terminated": return "已终止";
            case "expired": return "已到期";
            default: return "状态：" + status;
        }
    }
    
    /**
     * 自动生成从合同开始到当前月份的所有租金账单（入住确认后自动创建）
     * 账单状态为已确认，因为双方都已确认入住
     */
    private void generateAllRentBills(RentalTransaction transaction, RentalContract contract) {
        try {
            // 确定起始日期（入住日期或合同开始日期）
            LocalDate startDate = transaction.getCheckinDate();
            if (startDate == null) {
                startDate = contract.getStartDate();
            }
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            
            LocalDate endDate = LocalDate.now();
            LocalDate currentMonth = startDate.withDayOfMonth(1);
            
            int createdCount = 0;
            
            // 从入住月份开始，生成到当前月份的所有账单
            while (!currentMonth.isAfter(endDate)) {
                String period = currentMonth.getYear() + "年" + currentMonth.getMonthValue() + "月";
                
                // 检查该账期是否已有账单
                if (!checkBillExists(transaction.getTransactionId(), period)) {
                    createConfirmedBill(transaction, contract, period);
                    createdCount++;
                }
                
                currentMonth = currentMonth.plusMonths(1);
            }
            
            log.info("入住确认，自动生成租金账单: transactionId={}, 生成数量={}", 
                transaction.getTransactionId(), createdCount);
                
        } catch (Exception e) {
            log.error("生成租金账单失败: transactionId={}", transaction.getTransactionId(), e);
        }
    }
    
    /**
     * 检查指定账期是否已有账单
     */
    private boolean checkBillExists(Long transactionId, String period) {
        List<RentPaymentRecord> records = rentPaymentRecordMapper.findByTransactionId(transactionId);
        if (records == null || records.isEmpty()) {
            return false;
        }
        return records.stream()
            .anyMatch(r -> period.equals(r.getPaymentPeriod()) && "rent".equals(r.getPaymentType()));
    }
    
    /**
     * 创建已确认状态的租金账单
     */
    private void createConfirmedBill(RentalTransaction transaction, RentalContract contract, String period) {
        RentPaymentRecord record = new RentPaymentRecord();
        record.setTransactionId(transaction.getTransactionId());
        record.setContractId(contract.getContractId());
        record.setLandlordId(transaction.getLandlordId());
        record.setTenantId(transaction.getTenantId());
        record.setAmount(contract.getRentPrice());
        record.setPaymentType("rent");
        record.setPaymentPeriod(period);
        
        // 双方确认入住即视为确认支付
        record.setTenantConfirmed(true);
        record.setTenantConfirmTime(LocalDateTime.now());
        record.setLandlordConfirmed(true);
        record.setLandlordConfirmTime(LocalDateTime.now());
        record.setStatus("confirmed");
        record.setRemark("入住确认时系统自动生成");
        
        rentPaymentRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmComplete(Long transactionId, Long userId, LocalDate checkoutDate, String remark) {
        RentalTransaction transaction = getById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        boolean isLandlord = userId.equals(transaction.getLandlordId());
        boolean isTenant = userId.equals(transaction.getTenantId());

        if (!isLandlord && !isTenant) {
            throw new BusinessException("您无权操作此交易");
        }

        if (isLandlord) {
            transaction.setLandlordCompleteConfirm(true);
            transaction.setLandlordCompleteTime(LocalDateTime.now());
        } else {
            transaction.setTenantCompleteConfirm(true);
            transaction.setTenantCompleteTime(LocalDateTime.now());
        }

        if (checkoutDate != null) {
            transaction.setCheckoutDate(checkoutDate);
        }
        if (remark != null && !remark.isEmpty()) {
            transaction.setCheckoutRemark(remark);
        }

        // 检查双方是否都已确认完成
        if (Boolean.TRUE.equals(transaction.getLandlordCompleteConfirm()) 
                && Boolean.TRUE.equals(transaction.getTenantCompleteConfirm())) {
            transaction.setStatus("completed");
            log.info("双方确认交易完成: transactionId={}", transactionId);
            
            // 发送通知，提醒双方可以进行评价
            sendCompleteNotification(transaction);
        } else {
            transaction.setStatus("pending_complete");
        }

        updateById(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markEvaluated(Long transactionId, Long userId) {
        RentalTransaction transaction = getById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        boolean isLandlord = userId.equals(transaction.getLandlordId());
        boolean isTenant = userId.equals(transaction.getTenantId());

        if (!isLandlord && !isTenant) {
            throw new BusinessException("您无权操作此交易");
        }

        if (isLandlord) {
            transaction.setLandlordEvaluated(true);
        } else {
            transaction.setTenantEvaluated(true);
        }

        // 检查双方是否都已评价
        if (Boolean.TRUE.equals(transaction.getLandlordEvaluated()) 
                && Boolean.TRUE.equals(transaction.getTenantEvaluated())) {
            transaction.setStatus("evaluated");
            log.info("双方均已评价，交易状态变更为已评价: transactionId={}", transactionId);
        }

        updateById(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransaction(Long transactionId, Long userId, String reason) {
        RentalTransaction transaction = getById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 只有待签署或已签署状态可以取消
        if (!"pending_sign".equals(transaction.getStatus()) && !"signed".equals(transaction.getStatus())) {
            throw new BusinessException("当前状态不允许取消交易");
        }

        transaction.setStatus("cancelled");
        transaction.setCheckoutRemark("取消原因：" + reason);
        updateById(transaction);

        log.info("交易已取消: transactionId={}, userId={}, reason={}", transactionId, userId, reason);
    }

    @Override
    public RentalTransaction getByContractId(Long contractId) {
        return rentalTransactionMapper.findByContractId(contractId);
    }

    @Override
    public RentalTransaction getTransactionDetail(Long transactionId) {
        // 使用新的mapper方法获取完整信息
        RentalTransaction transaction = rentalTransactionMapper.findDetailById(transactionId);
        if (transaction != null) {
            // 同步交易状态与合同状态
            syncTransactionStatusWithContract(transaction);
            
            // 加载关联的合同信息
            RentalContract contract = rentalContractMapper.selectById(transaction.getContractId());
            transaction.setContract(contract);
        }
        return transaction;
    }
    
    /**
     * 同步交易状态与合同状态
     * 当合同终止或到期时，交易状态也需要相应更新
     */
    private void syncTransactionStatusWithContract(RentalTransaction transaction) {
        String contractStatus = transaction.getContractStatus();
        String transactionStatus = transaction.getStatus();
        
        // 如果合同已终止或到期，且交易还在进行中，则更新交易状态
        if ("terminated".equals(contractStatus) || "expired".equals(contractStatus)) {
            if ("signed".equals(transactionStatus) || "pending_checkin".equals(transactionStatus) 
                    || "living".equals(transactionStatus) || "pending_complete".equals(transactionStatus)) {
                // 更新交易状态为已完成
                transaction.setStatus("completed");
                updateById(transaction);
                log.info("合同{}，同步更新交易状态为已完成: transactionId={}", 
                    "terminated".equals(contractStatus) ? "已终止" : "已到期", 
                    transaction.getTransactionId());
            }
        }
    }

    /**
     * 发送入住确认通知
     */
    private void sendCheckinNotification(RentalTransaction transaction) {
        try {
            // 通知房东
            systemNotificationService.createNotification(
                transaction.getLandlordId(),
                "checkin_confirmed",
                "入住确认成功",
                "租客已入住，租赁交易正式开始，请双方遵守合同约定。",
                "transaction",
                transaction.getTransactionId()
            );
            
            // 通知租客
            systemNotificationService.createNotification(
                transaction.getTenantId(),
                "checkin_confirmed",
                "入住确认成功",
                "您已确认入住，租赁交易正式开始，请遵守合同约定，如有问题及时与房东沟通。",
                "transaction",
                transaction.getTransactionId()
            );
        } catch (Exception e) {
            log.error("发送入住确认通知失败", e);
        }
    }

    /**
     * 发送交易完成通知
     */
    private void sendCompleteNotification(RentalTransaction transaction) {
        try {
            // 通知房东
            systemNotificationService.createNotification(
                transaction.getLandlordId(),
                "transaction_completed",
                "交易已完成",
                "租赁交易已完成，您可以对租客进行评价，您的评价将帮助其他房东做出参考。",
                "transaction",
                transaction.getTransactionId()
            );
            
            // 通知租客
            systemNotificationService.createNotification(
                transaction.getTenantId(),
                "transaction_completed",
                "交易已完成",
                "租赁交易已完成，您可以对房东进行评价，您的评价将帮助其他租客做出参考。",
                "transaction",
                transaction.getTransactionId()
            );
        } catch (Exception e) {
            log.error("发送交易完成通知失败", e);
        }
    }
}
