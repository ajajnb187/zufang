package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.rentingmanagement.entity.RentalTransaction;

import java.time.LocalDate;
import java.util.List;

/**
 * 租赁交易服务接口
 */
public interface RentalTransactionService extends IService<RentalTransaction> {

    /**
     * 根据合同创建交易记录
     */
    RentalTransaction createFromContract(Long contractId);

    /**
     * 获取用户的交易列表
     */
    List<RentalTransaction> getUserTransactions(Long userId, String status);

    /**
     * 获取用户在租中的交易
     */
    List<RentalTransaction> getUserLivingTransactions(Long userId);

    /**
     * 确认入住（房东或租客）
     */
    void confirmCheckin(Long transactionId, Long userId, LocalDate checkinDate, String remark);

    /**
     * 确认交易完成（房东或租客）
     */
    void confirmComplete(Long transactionId, Long userId, LocalDate checkoutDate, String remark);

    /**
     * 标记已评价
     */
    void markEvaluated(Long transactionId, Long userId);

    /**
     * 取消交易
     */
    void cancelTransaction(Long transactionId, Long userId, String reason);

    /**
     * 根据合同ID获取交易
     */
    RentalTransaction getByContractId(Long contractId);

    /**
     * 获取交易详情
     */
    RentalTransaction getTransactionDetail(Long transactionId);
}
