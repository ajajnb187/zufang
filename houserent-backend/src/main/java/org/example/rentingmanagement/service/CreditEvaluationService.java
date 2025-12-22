package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.dto.CreditEvaluationRequest;
import org.example.rentingmanagement.entity.CreditEvaluation;

import java.math.BigDecimal;

/**
 * 信用评价服务接口
 */
public interface CreditEvaluationService {

    /**
     * 提交信用评价
     */
    CreditEvaluation submitEvaluation(CreditEvaluationRequest request, Long evaluatorId);

    /**
     * 获取用户收到的评价列表
     */
    IPage<CreditEvaluation> getReceivedEvaluations(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户发出的评价列表
     */
    IPage<CreditEvaluation> getSentEvaluations(Long evaluatorId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的平均评分
     */
    BigDecimal getAverageRating(Long userId);

    /**
     * 获取用户的评价统计信息
     */
    Object getEvaluationStatistics(Long userId);

    /**
     * 检查是否可以评价某个合同
     */
    boolean canEvaluate(Long contractId, Long evaluatorId);

    /**
     * 获取合同的双方评价
     */
    Object getContractEvaluations(Long contractId);

    /**
     * 更新用户信用分
     */
    void updateUserCreditScore(Long userId);
}
