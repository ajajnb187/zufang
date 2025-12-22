package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 论坛帖子创建请求DTO
 */
@Data
public class ForumPostCreateRequest {

    /**
     * 小区ID
     */
    @NotNull(message = "小区ID不能为空")
    private Long communityId;

    /**
     * 帖子类型
     */
    @NotBlank(message = "帖子类型不能为空")
    private String postType; // rent_need, share_info, discussion

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000字符")
    private String content;

    /**
     * 预算最小值（租房需求类型）
     */
    private BigDecimal budgetMin;

    /**
     * 预算最大值（租房需求类型）
     */
    private BigDecimal budgetMax;

    /**
     * 期望区域
     */
    private String expectedArea;

    /**
     * 期望户型
     */
    private String expectedType;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 是否紧急
     */
    private Boolean isUrgent = false;
}
