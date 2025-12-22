package org.example.rentingmanagement.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token权限认证配置类
 * 基于Sa-Token 1.44.0最新版本
 * 使用统一的SaInterceptor，支持注解鉴权和路由拦截
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册统一的Sa-Token拦截器，支持登录校验和注解鉴权
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                    // 登录校验 -- 拦截所有路由，并排除登录相关的路由用于开放访问
                    .match("/**")
                    .notMatch("/api/auth/wechat-login") // 微信登录
                    .notMatch("/api/auth/admin-login") // 管理员登录
                    .notMatch("/api/auth/admin-register") // 管理员注册
                    .notMatch("/api/auth/register") // 注册
                    .notMatch("/api/auth/dev-login") // 开发环境登录
                    .notMatch("/api/auth/test-accounts") // 测试账户列表
                    .notMatch("/api/public/**") // 公开接口
                    .notMatch("/api/houses/list") // 房源列表（游客可访问）
                    .notMatch("/api/houses/*/detail") // 房源详情（游客可访问）
                    .notMatch("/api/communities/list") // 小区列表
                    .notMatch("/websocket/**") // WebSocket连接
                    .notMatch("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs/**") // 接口文档
                    .notMatch("/favicon.ico", "/error") // 系统相关
                    .check(r -> StpUtil.checkLogin()); // 执行登录校验

        }).isAnnotation(true)) // 启用注解鉴权功能
        .addPathPatterns("/**");
    }
}
