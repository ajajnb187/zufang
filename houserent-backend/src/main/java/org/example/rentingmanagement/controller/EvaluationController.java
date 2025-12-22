package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.CreditEvaluation;
import org.example.rentingmanagement.entity.RentalTransaction;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.CreditEvaluationMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.RentalTransactionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final CreditEvaluationMapper creditEvaluationMapper;
    private final UserMapper userMapper;
    private final RentalTransactionService rentalTransactionService;

    /**
     * 提交评价
     */
    @PostMapping
    public Result<Void> submitEvaluation(@RequestBody Map<String, Object> data) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long targetUserId = Long.parseLong(data.get("targetUserId").toString());
            Long contractId = Long.parseLong(data.get("contractId").toString());
            BigDecimal rating = new BigDecimal(data.get("rating").toString());
            String tags = (String) data.get("tags");
            String comment = (String) data.get("comment");

            // 验证是否可以评价
            RentalTransaction transaction = rentalTransactionService.getByContractId(contractId);
            if (transaction == null) {
                return Result.error("交易记录不存在");
            }
            
            // 检查交易状态是否允许评价（已完成或已评价）
            if (!"completed".equals(transaction.getStatus()) && !"evaluated".equals(transaction.getStatus())) {
                return Result.error("当前交易状态不允许评价，请先完成交易");
            }

            // 检查是否已经评价过
            LambdaQueryWrapper<CreditEvaluation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CreditEvaluation::getContractId, contractId)
                   .eq(CreditEvaluation::getEvaluatorId, userId);
            if (creditEvaluationMapper.selectCount(wrapper) > 0) {
                return Result.error("您已经评价过了");
            }

            // 创建评价记录
            CreditEvaluation evaluation = new CreditEvaluation();
            evaluation.setContractId(contractId);
            evaluation.setEvaluatorId(userId);
            evaluation.setEvaluatedId(targetUserId);
            evaluation.setStarRating(rating);
            evaluation.setTags(tags);
            evaluation.setContent(comment);
            evaluation.setIsAnonymous(false);

            creditEvaluationMapper.insert(evaluation);

            // 更新被评价人的信用分
            updateUserCreditScore(targetUserId);

            // 标记交易已评价
            rentalTransactionService.markEvaluated(transaction.getTransactionId(), userId);

            log.info("评价提交成功: evaluatorId={}, evaluatedId={}, rating={}", userId, targetUserId, rating);
            return Result.success("评价提交成功");
        } catch (Exception e) {
            log.error("提交评价失败", e);
            return Result.error("提交评价失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收到的评价列表
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserEvaluations(@PathVariable Long userId) {
        try {
            LambdaQueryWrapper<CreditEvaluation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CreditEvaluation::getEvaluatedId, userId)
                   .orderByDesc(CreditEvaluation::getCreatedAt);
            List<CreditEvaluation> evaluations = creditEvaluationMapper.selectList(wrapper);

            // 计算平均评分
            BigDecimal avgRating = BigDecimal.ZERO;
            if (!evaluations.isEmpty()) {
                BigDecimal sum = evaluations.stream()
                        .map(CreditEvaluation::getStarRating)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                avgRating = sum.divide(new BigDecimal(evaluations.size()), 1, RoundingMode.HALF_UP);
            }

            // 填充评价人信息
            for (CreditEvaluation eval : evaluations) {
                if (!Boolean.TRUE.equals(eval.getIsAnonymous())) {
                    User evaluator = userMapper.selectById(eval.getEvaluatorId());
                    if (evaluator != null) {
                        eval.setEvaluatorName(evaluator.getNickname());
                        eval.setEvaluatorAvatar(evaluator.getAvatarUrl());
                    }
                } else {
                    eval.setEvaluatorName("匿名用户");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("evaluations", evaluations);
            result.put("avgRating", avgRating);
            result.put("totalCount", evaluations.size());

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户评价失败: userId={}", userId, e);
            return Result.error("获取评价失败");
        }
    }

    /**
     * 检查是否可以评价
     */
    @GetMapping("/check/{contractId}")
    public Result<Map<String, Object>> checkCanEvaluate(@PathVariable Long contractId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            RentalTransaction transaction = rentalTransactionService.getByContractId(contractId);
            if (transaction == null) {
                return Result.error("交易记录不存在");
            }

            Map<String, Object> result = new HashMap<>();
            
            // 检查交易状态
            boolean canEvaluate = "completed".equals(transaction.getStatus()) 
                    || "evaluated".equals(transaction.getStatus());
            result.put("canEvaluate", canEvaluate);
            
            if (!canEvaluate) {
                result.put("reason", "请先完成交易后再评价");
                return Result.success(result);
            }

            // 检查是否已经评价过
            LambdaQueryWrapper<CreditEvaluation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CreditEvaluation::getContractId, contractId)
                   .eq(CreditEvaluation::getEvaluatorId, userId);
            boolean hasEvaluated = creditEvaluationMapper.selectCount(wrapper) > 0;
            
            result.put("hasEvaluated", hasEvaluated);
            if (hasEvaluated) {
                result.put("canEvaluate", false);
                result.put("reason", "您已经评价过了");
            }

            // 确定评价对象
            Long targetUserId;
            if (userId.equals(transaction.getLandlordId())) {
                targetUserId = transaction.getTenantId();
            } else {
                targetUserId = transaction.getLandlordId();
            }
            result.put("targetUserId", targetUserId);

            return Result.success(result);
        } catch (Exception e) {
            log.error("检查评价状态失败: contractId={}", contractId, e);
            return Result.error("检查失败");
        }
    }

    /**
     * 更新用户信用分
     */
    private void updateUserCreditScore(Long userId) {
        try {
            LambdaQueryWrapper<CreditEvaluation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CreditEvaluation::getEvaluatedId, userId);
            List<CreditEvaluation> evaluations = creditEvaluationMapper.selectList(wrapper);

            if (!evaluations.isEmpty()) {
                BigDecimal sum = evaluations.stream()
                        .map(CreditEvaluation::getStarRating)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avgScore = sum.divide(new BigDecimal(evaluations.size()), 1, RoundingMode.HALF_UP);

                User user = userMapper.selectById(userId);
                if (user != null) {
                    user.setCreditScore(avgScore);
                    userMapper.updateById(user);
                    log.info("更新用户信用分: userId={}, creditScore={}", userId, avgScore);
                }
            }
        } catch (Exception e) {
            log.error("更新用户信用分失败: userId={}", userId, e);
        }
    }
}
