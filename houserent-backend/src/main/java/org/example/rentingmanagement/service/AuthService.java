package org.example.rentingmanagement.service;

import org.example.rentingmanagement.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 管理员登录
     */
    Object adminLogin(String username, String password);

    /**
     * 管理员注册
     */
    String adminRegister(String phone, String password, String adminType, Long communityId);

    /**
     * 微信小程序登录
     * @param code 微信授权码
     * @return token
     */
    String wechatLogin(String code);

    /**
     * 刷新Token
     */
    String refreshToken();

    /**
     * 注销登录
     */
    void logout();

    /**
     * 获取当前用户信息
     */
    User getCurrentUser();

    /**
     * 检查用户是否已认证小区
     */
    boolean isUserVerifiedInCommunity(Long userId, Long communityId);
}
