package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.CreditEvaluationRequest;
import org.example.rentingmanagement.entity.CreditEvaluation;
import org.example.rentingmanagement.service.CreditEvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 信用评价控制器
 */
@RestController
@RequestMapping("/api/credit-evaluations")
@RequiredArgsConstructor
public class CreditEvaluationController {

    private final CreditEvaluationService creditEvaluationService;

    /**
     * 提交信用评价
     */
    @PostMapping("/submit")
    public Result<CreditEvaluation> submitEvaluation(@RequestBody @Valid CreditEvaluationRequest request) {
        Long evaluatorId = StpUtil.getLoginIdAsLong();
        CreditEvaluation evaluation = creditEvaluationService.submitEvaluation(request, evaluatorId);
        return Result.success("评价提交成功", evaluation);
    }

    /**
     * 获取用户收到的评价列表
     */
    @GetMapping("/received")
    public Result<IPage<CreditEvaluation>> getReceivedEvaluations(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long targetUserId = userId != null ? userId : StpUtil.getLoginIdAsLong();
        IPage<CreditEvaluation> evaluations = creditEvaluationService.getReceivedEvaluations(targetUserId, pageNum, pageSize);
        return Result.success(evaluations);
    }

    /**
     * 获取用户发出的评价列表
     */
    @GetMapping("/sent")
    public Result<IPage<CreditEvaluation>> getSentEvaluations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long evaluatorId = StpUtil.getLoginIdAsLong();
        IPage<CreditEvaluation> evaluations = creditEvaluationService.getSentEvaluations(evaluatorId, pageNum, pageSize);
        return Result.success(evaluations);
    }

    /**
     * 获取用户的平均评分
     */
    @GetMapping("/average-rating")
    public Result<BigDecimal> getAverageRating(@RequestParam(required = false) Long userId) {
        Long targetUserId = userId != null ? userId : StpUtil.getLoginIdAsLong();
        BigDecimal avgRating = creditEvaluationService.getAverageRating(targetUserId);
        return Result.success(avgRating);
    }

    /**
     * 获取用户的评价统计
     */
    @GetMapping("/statistics")
    public Result<Object> getEvaluationStatistics(@RequestParam(required = false) Long userId) {
        Long targetUserId = userId != null ? userId : StpUtil.getLoginIdAsLong();
        Object statistics = creditEvaluationService.getEvaluationStatistics(targetUserId);
        return Result.success(statistics);
    }

    /**
     * 检查是否可以评价某个合同
     */
    @GetMapping("/check-can-evaluate")
    public Result<Boolean> checkCanEvaluate(@RequestParam Long contractId) {
        Long evaluatorId = StpUtil.getLoginIdAsLong();
        boolean canEvaluate = creditEvaluationService.canEvaluate(contractId, evaluatorId);
        return Result.success(canEvaluate);
    }

    /**
     * 获取合同的双方评价
     */
    @GetMapping("/contract/{contractId}")
    public Result<Object> getContractEvaluations(@PathVariable Long contractId) {
        Object evaluations = creditEvaluationService.getContractEvaluations(contractId);
        return Result.success(evaluations);
    }
}
