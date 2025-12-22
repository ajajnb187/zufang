package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.Feedback;
import org.example.rentingmanagement.mapper.FeedbackMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;

    /**
     * 提交用户反馈
     */
    @PostMapping("/submit")
    public Result<Void> submitFeedback(@RequestBody Feedback feedback) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            feedback.setUserId(userId);
            feedback.setStatus("pending");
            feedback.setCreatedAt(LocalDateTime.now());
            
            feedbackMapper.insert(feedback);
            
            log.info("用户{}提交反馈，类型：{}，内容：{}", userId, feedback.getType(), feedback.getContent());
            
            return Result.success("反馈提交成功，我们会尽快处理");
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return Result.error("提交失败，请重试");
        }
    }
}
