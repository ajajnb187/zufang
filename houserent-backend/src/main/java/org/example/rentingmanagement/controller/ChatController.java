package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.ChatMessage;
import org.example.rentingmanagement.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天管理控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取聊天历史记录（支持otherUserId或sessionId）
     */
    @GetMapping("/history")
    public Result<IPage<ChatMessage>> getChatHistory(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long otherUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 优先使用otherUserId直接查询，避免sessionId格式不匹配问题
        if (otherUserId != null) {
            IPage<ChatMessage> messages = chatService.getChatHistoryByUsers(userId, otherUserId, pageNum, pageSize);
            return Result.success(messages);
        }
        
        // 如果提供了sessionId，使用sessionId查询
        if (sessionId != null && !sessionId.isEmpty()) {
            IPage<ChatMessage> messages = chatService.getChatHistory(sessionId, userId, pageNum, pageSize);
            return Result.success(messages);
        }
        
        return Result.error("请提供sessionId或otherUserId");
    }

    /**
     * 获取用户未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read")
    public Result<Void> markMessagesAsRead(@RequestBody java.util.Map<String, Long> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long senderId = body.get("senderId");
        if (senderId == null) {
            return Result.error("senderId不能为空");
        }
        chatService.markMessagesAsRead(userId, senderId);
        return Result.success("消息已标记为已读");
    }

    /**
     * 获取最近聊天列表
     */
    @GetMapping("/recent")
    public Result<List<Object>> getRecentChats() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Object> chats = chatService.getRecentChats(userId);
        return Result.success(chats);
    }

    /**
     * 删除聊天记录
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteChatSession(@PathVariable String sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        chatService.deleteChatSession(sessionId, userId);
        return Result.success("聊天记录已删除");
    }

    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-count")
    public Result<Integer> getOnlineCount() {
        int count = chatService.getOnlineUserCount();
        return Result.success(count);
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/online-status")
    public Result<Boolean> checkUserOnline(@RequestParam Long userId) {
        boolean isOnline = chatService.isUserOnline(userId);
        return Result.success(isOnline);
    }
}
