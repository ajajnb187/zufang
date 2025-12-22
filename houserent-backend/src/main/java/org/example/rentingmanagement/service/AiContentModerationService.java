package org.example.rentingmanagement.service;

import org.example.rentingmanagement.entity.ChatMessage;
import org.example.rentingmanagement.entity.Violation;

/**
 * AI内容审核服务接口
 */
public interface AiContentModerationService {

    /**
     * 审核聊天消息内容
     * @param message 聊天消息
     * @return 如果违规返回Violation对象，否则返回null
     */
    Violation moderateMessage(ChatMessage message);

    /**
     * 批量审核历史消息
     * @param hours 审核最近多少小时的消息
     * @return 检测到的违规数量
     */
    int moderateRecentMessages(int hours);

    /**
     * 判断内容是否违规
     * @param content 内容
     * @return AI分析结果JSON
     */
    String analyzeContent(String content);

    /**
     * 定时审核任务
     */
    void scheduledModeration();

    /**
     * 异步审核消息
     * @param message 聊天消息
     */
    void moderateMessageAsync(ChatMessage message);
}
