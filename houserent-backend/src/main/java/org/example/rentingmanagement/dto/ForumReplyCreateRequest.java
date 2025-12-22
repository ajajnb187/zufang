package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 论坛回复创建请求DTO
 */
@Data
public class ForumReplyCreateRequest {

    /**
     * 父回复ID（如果是回复某条回复）
     */
    private Long parentReplyId;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容不能超过1000字符")
    private String content;

    /**
     * 图片列表
     */
    private List<String> images;
}
