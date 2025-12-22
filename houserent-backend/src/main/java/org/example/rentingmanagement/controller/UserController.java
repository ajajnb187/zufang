package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.UserMapper;
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
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<User> updateUserProfile(@RequestBody User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setUserId(userId);
        userMapper.updateById(user);
        User updatedUser = userMapper.selectById(userId);
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
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return Result.success("头像更新成功", avatarUrl);
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
}
