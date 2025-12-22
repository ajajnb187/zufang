package org.example.rentingmanagement.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.entity.Admin;
import org.example.rentingmanagement.entity.LandlordVerification;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.AdminMapper;
import org.example.rentingmanagement.mapper.LandlordVerificationMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SaToken权限接口实现类
 * 用于提供用户的角色和权限信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final LandlordVerificationMapper landlordVerificationMapper;

    /**
     * 返回用户所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 根据需要可以添加具体的权限逻辑
        List<String> permissions = new ArrayList<>();
        
        try {
            Long userId = Long.valueOf(loginId.toString());
            Admin admin = adminMapper.findByUserId(userId);
            
            if (admin != null && "active".equals(admin.getStatus())) {
                // 基于角色添加权限
                if ("platform".equals(admin.getAdminType())) {
                    permissions.add("platform:manage");
                    permissions.add("user:manage");
                    permissions.add("community:manage");
                } else if ("community".equals(admin.getAdminType())) {
                    permissions.add("community:manage");
                    permissions.add("house:audit");
                    permissions.add("contract:audit");
                }
            }
        } catch (Exception e) {
            log.error("获取用户权限失败: loginId={}", loginId, e);
        }
        
        return permissions;
    }

    /**
     * 返回用户所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        
        try {
            Long userId = Long.valueOf(loginId.toString());
            
            // 先检查是否是管理员
            Admin admin = adminMapper.findByUserId(userId);
            if (admin != null && "active".equals(admin.getStatus())) {
                roles.add("admin");
                
                if ("platform".equals(admin.getAdminType())) {
                    roles.add("platform_admin");
                } else if ("community".equals(admin.getAdminType())) {
                    roles.add("community_admin");
                }
                
                log.info("管理员{}的角色列表: {}", userId, roles);
                return roles;
            }
            
            // 检查是否是普通用户（租客/房东）
            User user = userMapper.selectById(userId);
            if (user != null && "active".equals(user.getStatus())) {
                // 租客角色（userType=3默认是租客）
                if (user.getUserType() == 3) {
                    roles.add("tenant");
                }
                
                // 房东角色（userType=4或有通过的房东认证）
                if (user.getUserType() == 4) {
                    roles.add("landlord");
                } else {
                    // 检查是否有通过的房东认证（支持同一账号双角色）
                    LambdaQueryWrapper<LandlordVerification> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(LandlordVerification::getUserId, userId)
                           .eq(LandlordVerification::getStatus, "approved");
                    LandlordVerification verification = landlordVerificationMapper.selectOne(wrapper);
                    
                    if (verification != null) {
                        roles.add("landlord");
                        log.info("用户{}通过房东认证，同时拥有租客和房东角色", userId);
                    }
                }
                
                log.info("用户{}的角色列表: {}", userId, roles);
                
                if (roles.isEmpty()) {
                    log.warn("用户{}的userType无效: {}", userId, user.getUserType());
                }
            } else {
                log.warn("用户{}不存在或状态异常", userId);
            }
        } catch (Exception e) {
            log.error("获取用户角色失败: loginId={}", loginId, e);
        }
        
        return roles;
    }
}
