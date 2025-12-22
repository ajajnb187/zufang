package org.example.rentingmanagement.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.entity.ChatMessage;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.entity.Violation;
import org.example.rentingmanagement.mapper.ChatMessageMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.mapper.ViolationMapper;
import org.example.rentingmanagement.service.AiContentModerationService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI内容审核服务实现类
 * 使用Spring AI Alibaba接入阿里云百炼qwen-plus模型进行违规内容检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiContentModerationServiceImpl implements AiContentModerationService {

    private final ChatModel chatModel;
    private final ChatMessageMapper chatMessageMapper;
    private final ViolationMapper violationMapper;
    private final UserMapper userMapper;

    private static final String MODERATION_SYSTEM_PROMPT = """
        你是一个专业的内容审核AI助手，负责检测租房平台聊天消息中的违规内容。
        
        违规类型包括：
        1. spam - 垃圾信息/广告：包含推广链接、无关广告、刷屏信息
        2. harassment - 骚扰辱骂：包含人身攻击、辱骂、威胁、歧视性言论
        3. fraud - 欺诈信息：虚假房源信息、诈骗信息、钓鱼信息
        4. illegal_content - 违法内容：涉及违法犯罪、色情、赌博、毒品等
        5. fake_info - 虚假信息：故意提供虚假的房源、价格、位置等信息
        6. privacy_leak - 隐私泄露：泄露他人身份证、银行卡、密码等敏感信息
        7. malicious_action - 恶意行为：恶意刷单、恶意差评、恶意投诉等
        
        请分析用户提供的消息内容，返回JSON格式的分析结果：
        {
            "isViolation": true/false,
            "violationType": "违规类型（如果有）",
            "severity": "low/medium/high/critical",
            "reason": "判断理由",
            "suggestion": "处理建议"
        }
        
        注意事项：
        - 正常的租房咨询、价格协商、看房预约等不算违规
        - 要区分正常的情绪表达和恶意骚扰
        - 严格判断，避免误判正常对话
        - 如果无法确定，倾向于判定为非违规
        """;

    @Override
    public Violation moderateMessage(ChatMessage message) {
        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            return null;
        }

        // 检查是否已经被审核过
        if (violationMapper.countByMessageId(message.getMessageId()) > 0) {
            log.debug("消息已被审核过: messageId={}", message.getMessageId());
            return null;
        }

        try {
            String analysisResult = analyzeContent(message.getContent());
            JSONObject result = JSON.parseObject(analysisResult);

            if (result != null && result.getBooleanValue("isViolation")) {
                Violation violation = new Violation();
                violation.setUserId(message.getSenderId());
                violation.setMessageId(message.getMessageId());
                violation.setViolationType(result.getString("violationType"));
                violation.setContent(message.getContent());
                violation.setAiAnalysis(analysisResult);
                violation.setSeverity(result.getString("severity"));
                violation.setStatus("pending");
                violation.setCreatedAt(LocalDateTime.now());

                violationMapper.insert(violation);
                log.info("检测到违规内容: userId={}, type={}, severity={}", 
                        message.getSenderId(), violation.getViolationType(), violation.getSeverity());

                // 根据严重程度自动处理
                autoHandleViolation(violation);

                return violation;
            }
        } catch (Exception e) {
            log.error("AI内容审核失败: messageId={}, error={}", message.getMessageId(), e.getMessage());
        }

        return null;
    }

    @Override
    public int moderateRecentMessages(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(ChatMessage::getCreatedAt, startTime);
        wrapper.eq(ChatMessage::getMessageType, "text");
        // 优先审核已被敏感词过滤标记的消息
        wrapper.orderByDesc(ChatMessage::getHasSensitive);
        wrapper.orderByDesc(ChatMessage::getCreatedAt);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        log.info("开始AI审核最近{}小时的消息，共{}条", hours, messages.size());

        int violationCount = 0;
        for (ChatMessage message : messages) {
            // 使用原始内容进行AI审核
            Violation violation = moderateMessage(message);
            if (violation != null) {
                violationCount++;
            }
        }

        log.info("AI审核完成，发现{}条违规内容", violationCount);
        return violationCount;
    }

    @Override
    public String analyzeContent(String content) {
        try {
            // 构建消息列表
            List<org.springframework.ai.chat.messages.Message> messages = List.of(
                new SystemMessage(MODERATION_SYSTEM_PROMPT),
                new UserMessage("请分析以下消息内容是否违规：\n\n" + content)
            );

            // 调用ChatModel
            Prompt prompt = new Prompt(messages);
            ChatResponse response = chatModel.call(prompt);

            if (response != null && response.getResult() != null) {
                String responseText = response.getResult().getOutput().getContent();
                log.debug("AI分析结果: {}", responseText);
                return extractJson(responseText);
            }
        } catch (Exception e) {
            log.error("调用AI分析失败: {}", e.getMessage(), e);
        }

        return "{\"isViolation\": false, \"reason\": \"分析失败，默认放行\"}";
    }

    /**
     * 从AI响应中提取JSON
     */
    private String extractJson(String text) {
        if (text == null) return null;
        
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }

    /**
     * 根据严重程度自动处理违规
     */
    private void autoHandleViolation(Violation violation) {
        String severity = violation.getSeverity();
        
        if ("critical".equals(severity)) {
            // 严重违规：自动封禁7天
            User user = userMapper.selectById(violation.getUserId());
            if (user != null) {
                user.setStatus("banned");
                userMapper.updateById(user);
            }
            violation.setActionTaken("auto_ban");
            violation.setBanUntil(LocalDateTime.now().plusDays(7));
            violation.setStatus("auto_processed");
            violation.setHandlerOpinion("AI自动处理：严重违规，自动封禁7天");
            violation.setHandledAt(LocalDateTime.now());
            violationMapper.updateById(violation);
            log.warn("严重违规，用户已被自动封禁: userId={}", violation.getUserId());
            
        } else if ("high".equals(severity)) {
            // 高危违规：自动禁言3天
            User user = userMapper.selectById(violation.getUserId());
            if (user != null) {
                user.setStatus("muted");
                userMapper.updateById(user);
            }
            violation.setActionTaken("auto_mute");
            violation.setBanUntil(LocalDateTime.now().plusDays(3));
            violation.setStatus("auto_processed");
            violation.setHandlerOpinion("AI自动处理：高危违规，自动禁言3天");
            violation.setHandledAt(LocalDateTime.now());
            violationMapper.updateById(violation);
            log.warn("高危违规，用户已被自动禁言: userId={}", violation.getUserId());
        }
        // 中低级别违规保持pending状态，等待人工审核
    }

    /**
     * 定时任务：每小时自动审核最近的消息
     */
    @Override
    @Scheduled(fixedRate = 3600000)
    @Async
    public void scheduledModeration() {
        log.info("执行定时AI审核任务...");
        try {
            moderateRecentMessages(1);
        } catch (Exception e) {
            log.error("定时AI审核任务失败: {}", e.getMessage());
        }
    }

    /**
     * 实时审核消息（供WebSocket调用）
     */
    @Override
    @Async
    public void moderateMessageAsync(ChatMessage message) {
        moderateMessage(message);
    }
}
