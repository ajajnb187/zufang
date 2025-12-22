package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.LoginRequest;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 管理员登录
     */
    @PostMapping("/admin-login")
    public Result<Object> adminLogin(@RequestBody @Valid LoginRequest request) {
        Object loginResult = authService.adminLogin(request.getUsername(), request.getPassword());
        return Result.success("登录成功", loginResult);
    }

    /**
     * 管理员注册（已禁用，请通过平台管理员后台新增小区管理员）
     */
    // @PostMapping("/admin-register")
    // public Result<String> adminRegister(@RequestBody @Valid LoginRequest request) {
    //     String result = authService.adminRegister(request.getPhone(), request.getPassword(), request.getAdminType(), request.getCommunityId());
    //     return Result.success("注册成功", result);
    // }

    /**
     * 微信小程序登录
     */
    @PostMapping("/wechat-login")
    public Result<String> wechatLogin(@RequestBody String requestBody) {
        log.info("=== 微信登录开始 ===");
        log.info("收到微信登录请求，原始数据: {}", requestBody);
        // 手动解析JSON，支持嵌套结构
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> map = mapper.readValue(requestBody, Map.class);
            
            // 获取code，可能是字符串或嵌套对象
            Object codeObj = map.get("code");
            String code;
            
            if (codeObj instanceof String) {
                // 直接是字符串
                code = (String) codeObj;
            } else if (codeObj instanceof Map) {
                // 嵌套结构 {"code": {"code": "xxx"}}
                Map<String, Object> codeMap = (Map<String, Object>) codeObj;
                code = (String) codeMap.get("code");
            } else {
                throw new IllegalArgumentException("无效的code格式");
            }
            
            log.info("解析得到code: {}", code);
            log.info("调用authService.wechatLogin...");
            String token = authService.wechatLogin(code);
            log.info("authService返回token: {}", token);
            log.info("token类型: {}", token != null ? token.getClass().getName() : "null");
            log.info("token长度: {}", token != null ? token.length() : 0);
            
            // 使用两参数版本，避免泛型匹配错误
            Result<String> result = Result.success("登录成功", token);
            log.info("构建Result对象完成");
            log.info("Result.code: {}", result.getCode());
            log.info("Result.message: {}", result.getMessage());
            log.info("Result.data: {}", result.getData());
            log.info("Result.data类型: {}", result.getData() != null ? result.getData().getClass().getName() : "null");
            log.info("=== 微信登录成功，返回Result ===");
            return result;
        } catch (Exception e) {
            log.error("=== 微信登录失败 ===", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public Result<String> refreshToken() {
        String token = authService.refreshToken();
        return Result.success("刷新成功", token);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("退出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check-login")
    public Result<Boolean> checkLogin() {
        boolean isLogin = StpUtil.isLogin();
        return Result.success(isLogin);
    }

    /**
     * 获取Token信息
     */
    @GetMapping("/token-info")
    public Result<Object> getTokenInfo() {
        Object tokenInfo = StpUtil.getTokenInfo();
        return Result.success(tokenInfo);
    }

    /**
     * 开发环境测试账号快捷登录（仅开发环境可用）
     * 前端传入user_id，直接完成登录，用于多账号测试
     */
    @PostMapping("/dev-login")
    public Result<Map<String, Object>> devLogin(@RequestBody Map<String, Object> request) {
        // 仅开发环境可用
        if (!"dev".equals(activeProfile)) {
            return Result.error("该接口仅在开发环境可用");
        }
        
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            log.info("开发环境测试登录，userId: {}", userId);
            
            // 直接使用SaToken登录
            StpUtil.login(userId);
            String token = StpUtil.getTokenValue();
            
            // 获取用户信息
            User user = authService.getCurrentUser();
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("token", token);
            result.put("user", user);
            
            log.info("测试账号登录成功，userId: {}, token: {}", userId, token);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("测试登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取测试账号列表（仅开发环境）
     */
    @GetMapping("/test-accounts")
    public Result<java.util.List<Map<String, Object>>> getTestAccounts() {
        if (!"dev".equals(activeProfile)) {
            return Result.error("该接口仅在开发环境可用");
        }
        
        java.util.List<Map<String, Object>> accounts = new java.util.ArrayList<>();
        
        // 添加测试账号信息 - userType: 3=租客, 4=房东
        accounts.add(createTestAccount(27L, "赵六（租客）", 3));
        accounts.add(createTestAccount(28L, "钱七（租客）", 3));
        accounts.add(createTestAccount(29L, "孙八（租客）", 3));
        accounts.add(createTestAccount(24L, "张三（房东）", 4));
        accounts.add(createTestAccount(25L, "李四（房东）", 4));
        accounts.add(createTestAccount(26L, "王五（房东）", 4));
        
        return Result.success(accounts);
    }
    
    private Map<String, Object> createTestAccount(Long userId, String name, Integer userType) {
        Map<String, Object> account = new java.util.HashMap<>();
        account.put("userId", userId);
        account.put("name", name);
        account.put("userType", userType);
        return account;
    }
}
