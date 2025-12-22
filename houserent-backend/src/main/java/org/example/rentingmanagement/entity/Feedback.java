package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 */
@Data
@TableName("feedback")
public class Feedback {
    
    @TableId(type = IdType.AUTO)
    private Long feedbackId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 反馈类型：bug-功能异常，suggestion-功能建议，service-服务问题，other-其他问题
     */
    private String type;
    
    /**
     * 反馈内容
     */
    private String content;
    
    /**
     * 联系方式
     */
    private String contact;
    
    /**
     * 附件图片（多张用逗号分隔）
     */
    private String images;
    
    /**
     * 状态：pending-待处理，processing-处理中，resolved-已解决
     */
    private String status;
    
    /**
     * 管理员回复
     */
    private String reply;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
