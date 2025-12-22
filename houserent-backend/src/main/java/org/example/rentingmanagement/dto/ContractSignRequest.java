package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 合同签名请求DTO
 */
@Data
public class ContractSignRequest {

    /**
     * 签名数据（Base64格式）
     * 注：合同ID从URL路径参数获取，不需要在请求体中传递
     */
    @NotBlank(message = "签名数据不能为空")
    private String signatureData;
}
