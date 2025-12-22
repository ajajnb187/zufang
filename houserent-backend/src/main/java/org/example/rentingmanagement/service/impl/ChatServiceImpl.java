package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.entity.ChatMessage;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.ChatMessageMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.ChatService;
import org.example.rentingmanagement.service.WebSocketChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final UserMapper userMapper;

    @Override
    public IPage<ChatMessage> getChatHistory(String sessionId, Long userId, Integer pageNum, Integer pageSize) {
        try {
            log.info("获取聊天历史: sessionId={}, userId={}, pageNum={}, pageSize={}", sessionId, userId, pageNum, pageSize);
            
            // 验证用户是否有权限查看该会话
            if (!isUserInSession(sessionId, userId)) {
                log.warn("用户无权查看会话: sessionId={}, userId={}", sessionId, userId);
                // 返回空结果而不是抛出异常，因为可能是新会话
                Page<ChatMessage> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }

            Page<ChatMessage> page = new Page<>(pageNum, pageSize);
            List<ChatMessage> messages = baseMapper.findSessionMessages(sessionId, pageSize, (int) page.offset());
            log.info("查询到消息数量: {}", messages != null ? messages.size() : 0);
            return page.setRecords(messages != null ? messages : new ArrayList<>());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取聊天历史失败: sessionId={}, userId={}", sessionId, userId, e);
            throw new BusinessException("获取聊天记录失败");
        }
    }

    @Override
    public IPage<ChatMessage> getChatHistoryByUsers(Long userId, Long otherUserId, Integer pageNum, Integer pageSize) {
        try {
            log.info("获取聊天历史(按用户ID): userId={}, otherUserId={}, pageNum={}, pageSize={}", userId, otherUserId, pageNum, pageSize);
            
            Page<ChatMessage> page = new Page<>(pageNum, pageSize);
            List<ChatMessage> messages = baseMapper.findMessagesBetweenUsers(userId, otherUserId, pageSize, (int) page.offset());
            log.info("查询到消息数量: {}", messages != null ? messages.size() : 0);
            
            return page.setRecords(messages != null ? messages : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取聊天历史失败: userId={}, otherUserId={}", userId, otherUserId, e);
            Page<ChatMessage> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        try {
            return baseMapper.getUnreadCount(userId);
        } catch (Exception e) {
            log.error("获取未读消息数量失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessagesAsRead(Long userId, Long senderId) {
        try {
            baseMapper.markMessagesAsRead(userId, senderId);
            log.info("消息标记为已读: userId={}, senderId={}", userId, senderId);
        } catch (Exception e) {
            log.error("标记消息已读失败: userId={}, senderId={}", userId, senderId, e);
            throw new BusinessException("标记消息失败");
        }
    }

    @Override
    public List<Object> getRecentChats(Long userId) {
        try {
            List<ChatMessage> recentChats = baseMapper.findRecentChats(userId);
            List<Object> result = new ArrayList<>();

            for (ChatMessage chat : recentChats) {
                Map<String, Object> chatInfo = new HashMap<>();
                chatInfo.put("sessionId", chat.getSessionId());

                // 获取对方用户信息
                Long otherUserId = chat.getSenderId().equals(userId) ? chat.getReceiverId() : chat.getSenderId();
                chatInfo.put("otherUserId", otherUserId);
                
                User otherUser = userMapper.selectById(otherUserId);
                if (otherUser != null) {
                    chatInfo.put("otherNickname", otherUser.getNickname());
                    chatInfo.put("otherAvatar", otherUser.getAvatarUrl());
                } else {
                    chatInfo.put("otherNickname", "用户" + otherUserId);
                    chatInfo.put("otherAvatar", null);
                }

                // 获取最后一条消息
                LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ChatMessage::getSessionId, chat.getSessionId())
                       .orderByDesc(ChatMessage::getCreatedAt)
                       .last("LIMIT 1");
                ChatMessage lastMessage = this.getOne(wrapper);
                if (lastMessage != null) {
                    // 优先返回过滤后的内容，如果没有则返回原始内容
                    String displayContent = lastMessage.getFilteredContent() != null 
                            ? lastMessage.getFilteredContent() 
                            : lastMessage.getContent();
                    chatInfo.put("lastMessage", displayContent);
                    chatInfo.put("lastTime", lastMessage.getCreatedAt());
                    chatInfo.put("lastMessageType", lastMessage.getMessageType());
                }

                // 获取未读消息数量
                LambdaQueryWrapper<ChatMessage> unreadWrapper = new LambdaQueryWrapper<>();
                unreadWrapper.eq(ChatMessage::getSessionId, chat.getSessionId())
                            .eq(ChatMessage::getReceiverId, userId)
                            .eq(ChatMessage::getIsRead, false);
                int unreadCount = Math.toIntExact(this.count(unreadWrapper));
                chatInfo.put("unreadCount", unreadCount);

                // 检查对方是否在线
                boolean isOnline = WebSocketChatService.isUserOnline(otherUserId);
                chatInfo.put("isOnline", isOnline);

                result.add(chatInfo);
            }

            return result;

        } catch (Exception e) {
            log.error("获取最近聊天列表失败: userId={}", userId, e);
            throw new BusinessException("获取聊天列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChatSession(String sessionId, Long userId) {
        try {
            // 验证用户权限
            if (!isUserInSession(sessionId, userId)) {
                throw new BusinessException("无权删除该会话");
            }

            // 逻辑删除：将用户在该会话中的消息标记为删除（可以考虑物理删除或者添加删除标记字段）
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getSessionId, sessionId)
                   .and(w -> w.eq(ChatMessage::getSenderId, userId).or().eq(ChatMessage::getReceiverId, userId));

            // 这里简单处理，实际项目中可以考虑添加软删除字段
            this.remove(wrapper);

            log.info("删除聊天会话成功: sessionId={}, userId={}", sessionId, userId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除聊天会话失败: sessionId={}, userId={}", sessionId, userId, e);
            throw new BusinessException("删除聊天记录失败");
        }
    }

    @Override
    public int getOnlineUserCount() {
        return WebSocketChatService.getOnlineCount();
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return WebSocketChatService.isUserOnline(userId);
    }

    @Override
    public void sendSystemMessage(Long userId, String message) {
        try {
            boolean sent = WebSocketChatService.sendMessageToUser(userId, message);
            if (sent) {
                log.info("系统消息发送成功: userId={}", userId);
            } else {
                log.info("用户不在线，系统消息发送失败: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("发送系统消息失败: userId={}", userId, e);
        }
    }

    @Override
    public String generateSessionId(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    /**
     * 检查用户是否在会话中
     */
    private boolean isUserInSession(String sessionId, Long userId) {
        try {
            // 会话ID格式: userId1_userId2
            String[] userIds = sessionId.split("_");
            if (userIds.length != 2) {
                return false;
            }

            Long user1Id = Long.parseLong(userIds[0]);
            Long user2Id = Long.parseLong(userIds[1]);

            return userId.equals(user1Id) || userId.equals(user2Id);

        } catch (Exception e) {
            log.error("检查用户会话权限失败: sessionId={}, userId={}", sessionId, userId, e);
            return false;
        }
    }
}
