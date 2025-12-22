package org.example.rentingmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 举报请求DTO
 */
@Data
public class ReportRequest {

    /**
     * 举报类型: house-虚假房源, user-不良用户, contract-合同违规, other-其他
     */
    @NotNull(message = "举报类型不能为空")
    private String reportType;

    /**
     * 被举报对象ID（房源ID或用户ID）
     */
    @NotNull(message = "被举报对象ID不能为空")
    private Long targetId;

    /**
     * 举报原因类型
     */
    @NotNull(message = "举报原因不能为空")
    private String reasonType;

    /**
     * 举报详细描述
     */
    private String reasonDetail;

    /**
     * 证据图片（多张用逗号分隔）
     */
    private String evidenceImages;
}
