package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 小区认证申请请求DTO
 */
@Data
public class CommunityVerificationRequest {

    /**
     * 小区ID
     */
    @NotNull(message = "小区ID不能为空")
    private Long communityId;

    /**
     * 证明类型
     */
    @NotBlank(message = "证明类型不能为空")
    private String proofType; // rental_contract, property_fee, ownership_cert, other

    /**
     * 证明材料图片URL列表
     */
    @NotEmpty(message = "请上传证明材料")
    private List<String> proofImages;

    /**
     * 申请理由
     */
    @NotBlank(message = "申请理由不能为空")
    private String applyReason;
}
