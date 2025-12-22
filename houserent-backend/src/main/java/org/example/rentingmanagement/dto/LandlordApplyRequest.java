package org.example.rentingmanagement.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 房东认证申请请求
 */
@Data
public class LandlordApplyRequest {
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", 
             message = "身份证号格式不正确")
    private String idCard;
    
    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFront;
    
    @NotBlank(message = "身份证反面照片不能为空")
    private String idCardBack;
    
    @NotBlank(message = "房产证明不能为空")
    private String propertyProof;
    
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private Long communityId;
}
