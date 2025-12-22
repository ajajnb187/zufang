package org.example.rentingmanagement.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 房东认证审核请求
 */
@Data
public class LandlordAuditRequest {
    
    @NotNull(message = "认证ID不能为空")
    private Long verificationId;
    
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    
    private String opinion;
    
    private String rejectReason;
}
