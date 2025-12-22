package org.example.rentingmanagement.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知推送服务
 * 通过WebSocket向用户推送系统通知（警告、封禁、解封等）
 */
@Slf4j
@Service
public class NotificationPushService {

    /**
     * 发送警告通知
     */
    public void sendWarningNotification(Long userId, String reason, String opinion) {
        String title = "账户警告通知";
        String content = "您的账户因违规行为收到警告。违规原因：" + reason +
                "。处理意见：" + (opinion != null ? opinion : "请遵守社区规范") +
                "。请注意规范您的言行，多次警告将导致账户被禁言或封禁。";

        Map<String, Object> extra = new HashMap<>();
        extra.put("status", "active");
        extra.put("action", "warning");

        pushNotification(userId, "warning", title, content, extra);
    }

    /**
     * 发送禁言通知
     */
    public void sendMuteNotification(Long userId, String reason, String opinion, LocalDateTime muteUntil) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String title = "账户禁言通知";
        String content = "您的账户因违规行为已被禁言。违规原因：" + reason +
                "。处理意见：" + (opinion != null ? opinion : "请遵守社区规范") +
                "。禁言截止：" + (muteUntil != null ? muteUntil.format(formatter) : "待定") +
                "。禁言期间您将无法发送聊天消息，但可正常浏览房源信息。";

        Map<String, Object> extra = new HashMap<>();
        extra.put("status", "muted");
        extra.put("action", "mute");
        extra.put("muteUntil", muteUntil != null ? muteUntil.format(formatter) : null);
        extra.put("canChat", false);
        extra.put("canBrowse", true);

        pushNotification(userId, "mute", title, content, extra);
    }

    /**
     * 发送封禁通知
     */
    public void sendBanNotification(Long userId, String reason, String opinion, LocalDateTime banUntil) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String title = "账户封禁通知";
        String content = "您的账户因严重违规行为已被封禁。违规原因：" + reason +
                "。处理意见：" + (opinion != null ? opinion : "严重违反社区规范") +
                "。封禁截止：" + (banUntil != null ? banUntil.format(formatter) : "永久封禁") +
                "。封禁期间您将无法使用本小程序的任何功能。如有疑问，请联系客服申诉。";

        Map<String, Object> extra = new HashMap<>();
        extra.put("status", "banned");
        extra.put("action", "ban");
        extra.put("banUntil", banUntil != null ? banUntil.format(formatter) : null);
        extra.put("canChat", false);
        extra.put("canBrowse", false);
        extra.put("forceLogout", true);

        pushNotification(userId, "ban", title, content, extra);
    }

    /**
     * 发送解封通知
     */
    public void sendUnbanNotification(Long userId, String opinion) {
        String title = "账户解封通知";
        String content = "您的账户已解除限制，现已恢复正常使用。解封说明：" +
                (opinion != null ? opinion : "管理员已解除限制") + "。请遵守社区规范，文明交流。";

        Map<String, Object> extra = new HashMap<>();
        extra.put("status", "active");
        extra.put("action", "unban");
        extra.put("canChat", true);
        extra.put("canBrowse", true);

        pushNotification(userId, "unban", title, content, extra);
    }

    /**
     * 通过WebSocket推送通知给用户
     */
    private void pushNotification(Long userId, String notificationType, String title, String content, Map<String, Object> extra) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "notification");
            message.put("notificationType", notificationType);
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());

            if (extra != null) {
                message.putAll(extra);
            }

            String jsonMessage = JSON.toJSONString(message);
            boolean sent = WebSocketChatService.sendMessageToUser(userId, jsonMessage);

            if (sent) {
                log.info("通知推送成功: userId={}, type={}", userId, notificationType);
            } else {
                log.debug("用户不在线，无法推送通知: userId={}, type={}", userId, notificationType);
            }

        } catch (Exception e) {
            log.error("推送通知失败: userId={}, error={}", userId, e.getMessage());
        }
    }
}
