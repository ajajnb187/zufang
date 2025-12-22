package org.example.rentingmanagement.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.CreditEvaluationRequest;
import org.example.rentingmanagement.entity.CreditEvaluation;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.CreditEvaluationMapper;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.CreditEvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用评价服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditEvaluationServiceImpl extends ServiceImpl<CreditEvaluationMapper, CreditEvaluation> implements CreditEvaluationService {

    private final RentalContractMapper rentalContractMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditEvaluation submitEvaluation(CreditEvaluationRequest request, Long evaluatorId) {
        try {
            // 验证合同是否存在
            RentalContract contract = rentalContractMapper.selectById(request.getContractId());
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            // 验证合同状态（只有已生效的合同才能评价）
            if (!"effective".equals(contract.getContractStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "只有生效的合同才能进行评价");
            }

            // 验证评价权限（只有合同当事人可以评价对方）
            Long evaluatedId = request.getEvaluatedId();
            if (!contract.getLandlordId().equals(evaluatorId) && !contract.getTenantId().equals(evaluatorId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权评价此合同");
            }

            // 验证被评价人是否是合同的另一方
            if (contract.getLandlordId().equals(evaluatorId) && !contract.getTenantId().equals(evaluatedId)) {
                throw new BusinessException("只能评价合同的另一方当事人");
            }
            if (contract.getTenantId().equals(evaluatorId) && !contract.getLandlordId().equals(evaluatedId)) {
                throw new BusinessException("只能评价合同的另一方当事人");
            }

            // 检查是否已经评价过
            if (baseMapper.hasEvaluated(request.getContractId(), evaluatorId)) {
                throw new BusinessException("您已对该合同进行过评价");
            }

            // 创建评价记录
            CreditEvaluation evaluation = new CreditEvaluation();
            evaluation.setContractId(request.getContractId());
            evaluation.setEvaluatorId(evaluatorId);
            evaluation.setEvaluatedId(evaluatedId);
            evaluation.setStarRating(request.getStarRating());
            evaluation.setContent(request.getContent());
            evaluation.setContractPerformance(request.getContractPerformance());
            evaluation.setTags(request.getTags() != null ? JSON.toJSONString(request.getTags()) : null);
            evaluation.setIsAnonymous(request.getIsAnonymous());

            this.save(evaluation);

            // 更新被评价人的信用分
            updateUserCreditScore(evaluatedId);

            log.info("信用评价提交成功: contractId={}, evaluatorId={}, evaluatedId={}",
                    request.getContractId(), evaluatorId, evaluatedId);
            return evaluation;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交信用评价失败: {}", e.getMessage(), e);
            throw new BusinessException("提交评价失败");
        }
    }

    @Override
    public IPage<CreditEvaluation> getReceivedEvaluations(Long userId, Integer pageNum, Integer pageSize) {
        try {
            Page<CreditEvaluation> page = new Page<>(pageNum, pageSize);
            List<CreditEvaluation> evaluations = baseMapper.findByEvaluatedId(userId);
            return page.setRecords(evaluations);
        } catch (Exception e) {
            log.error("获取收到的评价失败: userId={}", userId, e);
            throw new BusinessException("获取评价列表失败");
        }
    }

    @Override
    public IPage<CreditEvaluation> getSentEvaluations(Long evaluatorId, Integer pageNum, Integer pageSize) {
        try {
            Page<CreditEvaluation> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<CreditEvaluation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CreditEvaluation::getEvaluatorId, evaluatorId)
                    .orderByDesc(CreditEvaluation::getCreatedAt);
            return this.page(page, wrapper);
        } catch (Exception e) {
            log.error("获取发出的评价失败: evaluatorId={}", evaluatorId, e);
            throw new BusinessException("获取评价列表失败");
        }
    }

    @Override
    public BigDecimal getAverageRating(Long userId) {
        try {
            BigDecimal avgRating = baseMapper.getAverageRating(userId);
            return avgRating != null ? avgRating.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取平均评分失败: userId={}", userId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Object getEvaluationStatistics(Long userId) {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总评价数
            int totalCount = baseMapper.getEvaluationCount(userId);
            statistics.put("totalCount", totalCount);

            // 平均评分
            BigDecimal avgRating = getAverageRating(userId);
            statistics.put("averageRating", avgRating);

            // 各星级分布
            List<CreditEvaluation> evaluations = baseMapper.findByEvaluatedId(userId);
            Map<String, Integer> ratingDistribution = new HashMap<>();
            ratingDistribution.put("5star", 0);
            ratingDistribution.put("4star", 0);
            ratingDistribution.put("3star", 0);
            ratingDistribution.put("2star", 0);
            ratingDistribution.put("1star", 0);

            for (CreditEvaluation evaluation : evaluations) {
                int rating = evaluation.getStarRating().intValue();
                String key = rating + "star";
                ratingDistribution.put(key, ratingDistribution.get(key) + 1);
            }
            statistics.put("ratingDistribution", ratingDistribution);

            // 好评率（4星及以上）
            int goodRatingCount = ratingDistribution.get("5star") + ratingDistribution.get("4star");
            double goodRatingRate = totalCount > 0 ? (double) goodRatingCount / totalCount * 100 : 0;
            statistics.put("goodRatingRate", Math.round(goodRatingRate * 10) / 10.0);

            return statistics;
        } catch (Exception e) {
            log.error("获取评价统计失败: userId={}", userId, e);
            throw new BusinessException("获取评价统计失败");
        }
    }

    @Override
    public boolean canEvaluate(Long contractId, Long evaluatorId) {
        try {
            // 检查合同是否存在
            RentalContract contract = rentalContractMapper.selectById(contractId);
            if (contract == null) {
                return false;
            }

            // 检查合同状态
            if (!"effective".equals(contract.getContractStatus())) {
                return false;
            }

            // 检查是否是合同当事人
            if (!contract.getLandlordId().equals(evaluatorId) && !contract.getTenantId().equals(evaluatorId)) {
                return false;
            }

            // 检查是否已评价过
            return !baseMapper.hasEvaluated(contractId, evaluatorId);

        } catch (Exception e) {
            log.error("检查评价权限失败: contractId={}, evaluatorId={}", contractId, evaluatorId, e);
            return false;
        }
    }

    @Override
    public Object getContractEvaluations(Long contractId) {
        try {
            List<CreditEvaluation> evaluations = baseMapper.findByContractId(contractId);

            Map<String, Object> result = new HashMap<>();
            result.put("evaluations", evaluations);
            result.put("count", evaluations.size());

            return result;
        } catch (Exception e) {
            log.error("获取合同评价失败: contractId={}", contractId, e);
            throw new BusinessException("获取合同评价失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCreditScore(Long userId) {
        try {
            BigDecimal avgRating = getAverageRating(userId);
            int evaluationCount = baseMapper.getEvaluationCount(userId);

            // 计算信用分（基于平均评分和评价数量）
            BigDecimal creditScore;
            if (evaluationCount == 0) {
                creditScore = BigDecimal.valueOf(5.0); // 默认5分
            } else {
                // 信用分 = 平均评分 * 权重系数
                double weightFactor = Math.min(1.0, evaluationCount / 10.0); // 评价数越多权重越高，最高1.0
                creditScore = avgRating.multiply(BigDecimal.valueOf(weightFactor));
                creditScore = creditScore.setScale(1, RoundingMode.HALF_UP);
            }

            // 更新用户信用分
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setCreditScore(creditScore);
                userMapper.updateById(user);
                log.info("更新用户信用分: userId={}, creditScore={}", userId, creditScore);
            }

        } catch (Exception e) {
            log.error("更新用户信用分失败: userId={}", userId, e);
            // 信用分更新失败不抛出异常，不影响主流程
        }
    }
}
