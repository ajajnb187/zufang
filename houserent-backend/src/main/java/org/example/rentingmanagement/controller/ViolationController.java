package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.mapper.ViolationMapper;
import org.example.rentingmanagement.entity.Violation;
import org.example.rentingmanagement.service.AiContentModerationService;
import org.example.rentingmanagement.service.NotificationPushService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 违规处理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/violations")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class ViolationController {

    private final ViolationMapper violationMapper;
    private final UserMapper userMapper;
    private final AiContentModerationService aiContentModerationService;
    private final NotificationPushService notificationPushService;

    /**
     * 获取违规记录列表
     */
    @GetMapping
    public Result<Map<String, Object>> getViolationList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("获取违规记录列表: type={}, status={}, severity={}", type, status, severity);

        LambdaQueryWrapper<Violation> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Violation::getViolationType, type);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Violation::getStatus, status);
        }
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(Violation::getSeverity, severity);
        }
        wrapper.orderByDesc(Violation::getCreatedAt);

        Page<Violation> page = new Page<>(pageNum, pageSize);
        Page<Violation> result = violationMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Violation v : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getViolationId());
            item.put("type", v.getViolationType());
            item.put("description", v.getContent());
            item.put("aiAnalysis", v.getAiAnalysis());
            item.put("severity", v.getSeverity());
            item.put("status", v.getStatus());
            item.put("actionTaken", v.getActionTaken());
            item.put("handlerOpinion", v.getHandlerOpinion());
            item.put("createdAt", v.getCreatedAt() != null ? v.getCreatedAt().format(formatter) : null);
            item.put("handledAt", v.getHandledAt() != null ? v.getHandledAt().format(formatter) : null);
            item.put("banUntil", v.getBanUntil() != null ? v.getBanUntil().format(formatter) : null);

            User user = userMapper.selectById(v.getUserId());
            if (user != null) {
                item.put("userId", user.getUserId());
                item.put("targetUser", user.getNickname());
                item.put("userPhone", user.getPhone());
                item.put("userStatus", user.getStatus());
            } else {
                item.put("targetUser", "未知用户");
            }

            if (v.getHandlerId() != null) {
                User handler = userMapper.selectById(v.getHandlerId());
                item.put("handlerName", handler != null ? handler.getNickname() : "未知");
            }

            records.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());

        return Result.success(data);
    }

    /**
     * 获取违规详情
     */
    @GetMapping("/{violationId}")
    public Result<Map<String, Object>> getViolationDetail(@PathVariable Long violationId) {
        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            return Result.error("违规记录不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", violation.getViolationId());
        detail.put("type", violation.getViolationType());
        detail.put("content", violation.getContent());
        detail.put("aiAnalysis", violation.getAiAnalysis());
        detail.put("severity", violation.getSeverity());
        detail.put("status", violation.getStatus());
        detail.put("actionTaken", violation.getActionTaken());
        detail.put("handlerOpinion", violation.getHandlerOpinion());
        detail.put("createdAt", violation.getCreatedAt());
        detail.put("handledAt", violation.getHandledAt());
        detail.put("banUntil", violation.getBanUntil());

        User user = userMapper.selectById(violation.getUserId());
        if (user != null) {
            detail.put("userId", user.getUserId());
            detail.put("userName", user.getNickname());
            detail.put("userPhone", user.getPhone());
            detail.put("userStatus", user.getStatus());
            detail.put("userAvatar", user.getAvatarUrl());
        }

        return Result.success(detail);
    }

    /**
     * 处理违规 - 警告
     */
    @PostMapping("/{violationId}/warn")
    public Result<String> warnUser(@PathVariable Long violationId, @RequestBody Map<String, String> body) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        String opinion = body.get("opinion");

        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            return Result.error("违规记录不存在");
        }

        violation.setStatus("resolved");
        violation.setActionTaken("warning");
        violation.setHandlerId(handlerId);
        violation.setHandlerOpinion(opinion);
        violation.setHandledAt(LocalDateTime.now());
        violationMapper.updateById(violation);

        // 推送警告通知给用户
        notificationPushService.sendWarningNotification(
            violation.getUserId(),
            violation.getContent(),
            opinion
        );

        log.info("违规处理-警告: violationId={}, handlerId={}", violationId, handlerId);
        return Result.success("已对用户发出警告，通知已推送");
    }

    /**
     * 处理违规 - 禁言
     */
    @PostMapping("/{violationId}/mute")
    public Result<String> muteUser(@PathVariable Long violationId, @RequestBody Map<String, Object> body) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        String opinion = (String) body.get("opinion");
        Integer days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 1;

        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            return Result.error("违规记录不存在");
        }

        User user = userMapper.selectById(violation.getUserId());
        if (user != null) {
            user.setStatus("muted");
            userMapper.updateById(user);
        }

        LocalDateTime banUntil = LocalDateTime.now().plusDays(days);
        violation.setStatus("resolved");
        violation.setActionTaken("mute");
        violation.setBanUntil(banUntil);
        violation.setHandlerId(handlerId);
        violation.setHandlerOpinion(opinion);
        violation.setHandledAt(LocalDateTime.now());
        violationMapper.updateById(violation);

        // 推送禁言通知给用户
        notificationPushService.sendMuteNotification(
            violation.getUserId(),
            violation.getContent(),
            opinion,
            banUntil
        );

        log.info("违规处理-禁言: violationId={}, userId={}, days={}", violationId, violation.getUserId(), days);
        return Result.success("用户已被禁言" + days + "天，通知已推送");
    }

    /**
     * 处理违规 - 封禁
     */
    @PostMapping("/{violationId}/ban")
    public Result<String> banUser(@PathVariable Long violationId, @RequestBody Map<String, Object> body) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        String opinion = (String) body.get("opinion");
        Integer days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 30;

        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            return Result.error("违规记录不存在");
        }

        User user = userMapper.selectById(violation.getUserId());
        if (user != null) {
            user.setStatus("banned");
            userMapper.updateById(user);
        }

        LocalDateTime banUntil = LocalDateTime.now().plusDays(days);
        violation.setStatus("resolved");
        violation.setActionTaken("ban");
        violation.setBanUntil(banUntil);
        violation.setHandlerId(handlerId);
        violation.setHandlerOpinion(opinion);
        violation.setHandledAt(LocalDateTime.now());
        violationMapper.updateById(violation);

        // 推送封禁通知给用户
        notificationPushService.sendBanNotification(
            violation.getUserId(),
            violation.getContent(),
            opinion,
            banUntil
        );

        log.info("违规处理-封禁: violationId={}, userId={}, days={}", violationId, violation.getUserId(), days);
        return Result.success("用户已被封禁" + days + "天，通知已推送");
    }

    /**
     * 处理违规 - 驳回
     */
    @PostMapping("/{violationId}/dismiss")
    public Result<String> dismissViolation(@PathVariable Long violationId, @RequestBody Map<String, String> body) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        String opinion = body.get("opinion");

        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            return Result.error("违规记录不存在");
        }

        violation.setStatus("dismissed");
        violation.setActionTaken("none");
        violation.setHandlerId(handlerId);
        violation.setHandlerOpinion(opinion != null ? opinion : "AI误判，已驳回");
        violation.setHandledAt(LocalDateTime.now());
        violationMapper.updateById(violation);

        log.info("违规处理-驳回: violationId={}, handlerId={}", violationId, handlerId);
        return Result.success("违规记录已驳回");
    }

    /**
     * 解除封禁
     */
    @PostMapping("/{violationId}/unban")
    public Result<String> unbanUser(@PathVariable Long violationId, @RequestBody Map<String, String> body) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        String opinion = body.get("opinion");

        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            return Result.error("违规记录不存在");
        }

        User user = userMapper.selectById(violation.getUserId());
        if (user != null) {
            user.setStatus("active");
            userMapper.updateById(user);
        }

        violation.setBanUntil(null);
        violation.setHandlerOpinion((violation.getHandlerOpinion() != null ? violation.getHandlerOpinion() + " | " : "") 
                + "已解封: " + (opinion != null ? opinion : "管理员操作"));
        violation.setHandledAt(LocalDateTime.now());
        violationMapper.updateById(violation);

        // 推送解封通知给用户
        notificationPushService.sendUnbanNotification(
            violation.getUserId(),
            opinion
        );

        log.info("用户解封: violationId={}, userId={}", violationId, violation.getUserId());
        return Result.success("用户已解除封禁，通知已推送");
    }

    /**
     * 触发AI审核
     */
    @PostMapping("/scan")
    public Result<Map<String, Object>> triggerAiScan(@RequestBody Map<String, Integer> body) {
        Integer hours = body.getOrDefault("hours", 24);
        
        log.info("手动触发AI审核: hours={}", hours);
        int violationCount = aiContentModerationService.moderateRecentMessages(hours);

        Map<String, Object> result = new HashMap<>();
        result.put("scannedHours", hours);
        result.put("violationsFound", violationCount);

        return Result.success(result);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getViolationStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", violationMapper.selectCount(null));

        LambdaQueryWrapper<Violation> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Violation::getStatus, "pending");
        stats.put("pending", violationMapper.selectCount(pendingWrapper));

        LambdaQueryWrapper<Violation> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Violation::getCreatedAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        stats.put("today", violationMapper.selectCount(todayWrapper));

        Map<String, Long> typeStats = new HashMap<>();
        for (String type : Arrays.asList("fake_info", "illegal_content", "spam", "harassment", "malicious_action", "other")) {
            LambdaQueryWrapper<Violation> typeWrapper = new LambdaQueryWrapper<>();
            typeWrapper.eq(Violation::getViolationType, type);
            typeStats.put(type, violationMapper.selectCount(typeWrapper));
        }
        stats.put("byType", typeStats);

        return Result.success(stats);
    }
}
