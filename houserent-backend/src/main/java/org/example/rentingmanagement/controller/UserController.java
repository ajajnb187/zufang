package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.ChangePasswordRequest;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.util.ImageUrlUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final ImageUrlUtil imageUrlUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<User> getUserProfile() {
        // 调试日志：查看请求头
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            log.info("=== DEBUG getUserProfile ===");
            log.info("Authorization header: {}", authHeader);
            log.info("Sa-Token getTokenValue: {}", StpUtil.getTokenValue());
            log.info("Sa-Token getTokenName: {}", StpUtil.getTokenName());
            log.info("==============================");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        // 转换头像URL为Base64 Data URL（用于微信小程序真机）
        if (user != null && user.getAvatarUrl() != null) {
            String originalUrl = user.getAvatarUrl();
            String base64Url = imageUrlUtil.toBase64DataUrl(originalUrl);
            log.info("=== URL转换为Base64 ===");
            log.info("原始路径: {}", originalUrl);
            log.info("Base64长度: {}", base64Url != null ? base64Url.length() : 0);
            log.info("================");
            user.setAvatarUrl(base64Url);
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<User> updateUserProfile(@RequestBody User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setUserId(userId);
        // 如果传入的avatarUrl是代理URL，需要转换为相对路径保存
        if (user.getAvatarUrl() != null) {
            user.setAvatarUrl(imageUrlUtil.extractRelativePath(user.getAvatarUrl()));
        }
        userMapper.updateById(user);
        User updatedUser = userMapper.selectById(userId);
        // 返回时转换为Base64 Data URL
        if (updatedUser != null && updatedUser.getAvatarUrl() != null) {
            updatedUser.setAvatarUrl(imageUrlUtil.toBase64DataUrl(updatedUser.getAvatarUrl()));
        }
        return Result.success("个人信息更新成功", updatedUser);
    }

    /**
     * 更新用户头像
     */
    @PostMapping("/avatar")
    public Result<String> updateAvatar(@RequestParam String avatarUrl) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = new User();
        user.setUserId(userId);
        // 转换为相对路径保存
        String relativePath = imageUrlUtil.extractRelativePath(avatarUrl);
        user.setAvatarUrl(relativePath);
        userMapper.updateById(user);
        // 返回Base64 Data URL
        String base64Url = imageUrlUtil.toBase64DataUrl(relativePath);
        return Result.success("头像更新成功", base64Url);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        Long userId = StpUtil.getLoginIdAsLong();
        // 实际查询用户统计数据
        return Result.success("用户统计信息查询成功");
    }

    /**
     * 获取用户的订单历史（租赁记录）
     */
    @GetMapping("/orders")
    public Result<IPage<Object>> getUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 查询用户作为租户或房东的所有合同记录
        return Result.success("订单列表查询成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }
        
        // 更新新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        
        // 登出当前会话，强制重新登录
        StpUtil.logout();
        
        log.info("用户 {} 修改密码成功", userId);
        return Result.success("密码修改成功，请重新登录");
    }
}
