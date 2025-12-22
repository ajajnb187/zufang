package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    /**
     * 微信授权码（微信登录用）
     */
    private String code;

    /**
     * 加密数据（微信登录用）
     */
    private String encryptedData;

    /**
     * 初始向量（微信登录用）
     */
    private String iv;

    /**
     * 用户类型（微信登录时设置，3-租户，4-房东）
     */
    private Integer userType;

    /**
     * 用户名（管理员登录用）
     */
    private String username;

    /**
     * 密码（管理员登录用）
     */
    private String password;

    /**
     * 管理员类型（管理员注册用）
     */
    private String adminType;

    /**
     * 手机号（管理员注册用）
     */
    private String phone;

    /**
     * 昵称（管理员注册用）
     */
    private String nickname;

    /**
     * 小区ID（小区管理员注册用）
     */
    private Long communityId;
}
