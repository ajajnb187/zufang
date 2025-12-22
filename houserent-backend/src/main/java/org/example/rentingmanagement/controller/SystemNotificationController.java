package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.SystemNotification;
import org.example.rentingmanagement.service.SystemNotificationService;
import org.springframework.web.bind.annotation.*;

/**
 * 系统通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    public Result<IPage<SystemNotification>> getUserNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<SystemNotification> notifications = systemNotificationService.getUserNotifications(userId, pageNum, pageSize);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = systemNotificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/{notificationId}/read")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        systemNotificationService.markAsRead(notificationId, userId);
        return Result.success("通知已标记为已读");
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/mark-all-read")
    public Result<Void> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        systemNotificationService.markAllAsRead(userId);
        return Result.success("所有通知已标记为已读");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public Result<Void> deleteNotification(@PathVariable Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        systemNotificationService.deleteNotification(notificationId, userId);
        return Result.success("通知已删除");
    }
}
