package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.Admin;
import org.example.rentingmanagement.entity.AdminPermission;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.AdminMapper;
import org.example.rentingmanagement.mapper.AdminPermissionMapper;
import org.example.rentingmanagement.mapper.CommunityMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 权限管控控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/admin-permissions")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class PermissionController {

    private final AdminPermissionMapper permissionMapper;
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final CommunityMapper communityMapper;

    /**
     * 获取权限配置列表
     */
    @GetMapping
    public Result<Map<String, Object>> getPermissionList(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String adminName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("获取权限配置列表: communityId={}, adminName={}", communityId, adminName);

        // 查询所有小区管理员
        LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(Admin::getAdminType, "community");
        if (communityId != null) {
            adminWrapper.eq(Admin::getCommunityId, communityId);
        }
        List<Admin> adminList = adminMapper.selectList(adminWrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Admin admin : adminList) {
            User user = userMapper.selectById(admin.getUserId());
            String nickname = user != null ? user.getNickname() : "未知";

            if (adminName != null && !adminName.isEmpty() && !nickname.contains(adminName)) {
                continue;
            }

            // 查询该管理员的权限配置
            LambdaQueryWrapper<AdminPermission> permWrapper = new LambdaQueryWrapper<>();
            permWrapper.eq(AdminPermission::getAdminId, admin.getAdminId());
            AdminPermission permission = permissionMapper.selectOne(permWrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("id", permission != null ? permission.getPermissionId() : null);
            item.put("adminId", admin.getAdminId());
            item.put("adminName", nickname);
            item.put("phone", user != null ? user.getPhone() : "");

            if (admin.getCommunityId() != null) {
                Community community = communityMapper.selectById(admin.getCommunityId());
                item.put("communityId", admin.getCommunityId());
                item.put("communityName", community != null ? community.getCommunityName() : "未分配");
            } else {
                item.put("communityName", "未分配");
            }

            if (permission != null) {
                item.put("maxHouseAudit", permission.getMaxHouseAudit());
                item.put("maxUserAudit", permission.getMaxUserAudit());
                item.put("maxContractAudit", permission.getMaxContractAudit());
                item.put("canManageFacilities", permission.getCanManageFacilities());
                item.put("canHandleReports", permission.getCanHandleReports());
                item.put("canHandleViolations", permission.getCanHandleViolations());
                item.put("status", permission.getStatus());
                item.put("remarks", permission.getRemarks());
                item.put("createdAt", permission.getCreatedAt() != null ? permission.getCreatedAt().format(formatter) : null);
            } else {
                item.put("maxHouseAudit", 50);
                item.put("maxUserAudit", 30);
                item.put("maxContractAudit", 20);
                item.put("canManageFacilities", true);
                item.put("canHandleReports", true);
                item.put("canHandleViolations", true);
                item.put("status", admin.getStatus());
                item.put("remarks", "");
            }

            records.add(item);
        }

        // 手动分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, records.size());
        List<Map<String, Object>> pagedList = start < records.size() ? records.subList(start, end) : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pagedList);
        data.put("total", records.size());
        data.put("current", pageNum);
        data.put("size", pageSize);

        return Result.success(data);
    }

    /**
     * 创建权限配置
     */
    @PostMapping
    public Result<String> createPermission(@RequestBody Map<String, Object> body) {
        Long adminId = body.get("adminId") != null ? Long.parseLong(body.get("adminId").toString()) : null;
        if (adminId == null) {
            return Result.error("请选择管理员");
        }

        // 检查是否已存在
        LambdaQueryWrapper<AdminPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminPermission::getAdminId, adminId);
        if (permissionMapper.selectCount(wrapper) > 0) {
            return Result.error("该管理员已存在权限配置");
        }

        AdminPermission permission = new AdminPermission();
        permission.setAdminId(adminId);
        permission.setMaxHouseAudit(body.get("maxHouseAudit") != null ? Integer.parseInt(body.get("maxHouseAudit").toString()) : 50);
        permission.setMaxUserAudit(body.get("maxUserAudit") != null ? Integer.parseInt(body.get("maxUserAudit").toString()) : 30);
        permission.setMaxContractAudit(body.get("maxContractAudit") != null ? Integer.parseInt(body.get("maxContractAudit").toString()) : 20);
        permission.setCanManageFacilities(body.get("canManageFacilities") != null ? (Boolean) body.get("canManageFacilities") : true);
        permission.setCanHandleReports(body.get("canHandleReports") != null ? (Boolean) body.get("canHandleReports") : true);
        permission.setCanHandleViolations(body.get("canHandleViolations") != null ? (Boolean) body.get("canHandleViolations") : true);
        permission.setStatus("active");
        permission.setRemarks((String) body.get("remarks"));
        permission.setCreatedAt(LocalDateTime.now());

        permissionMapper.insert(permission);
        log.info("创建权限配置: adminId={}", adminId);
        return Result.success("权限配置创建成功");
    }

    /**
     * 更新权限配置
     */
    @PutMapping("/{permissionId}")
    public Result<String> updatePermission(@PathVariable Long permissionId, @RequestBody Map<String, Object> body) {
        AdminPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return Result.error("权限配置不存在");
        }

        if (body.containsKey("maxHouseAudit")) {
            permission.setMaxHouseAudit(Integer.parseInt(body.get("maxHouseAudit").toString()));
        }
        if (body.containsKey("maxUserAudit")) {
            permission.setMaxUserAudit(Integer.parseInt(body.get("maxUserAudit").toString()));
        }
        if (body.containsKey("maxContractAudit")) {
            permission.setMaxContractAudit(Integer.parseInt(body.get("maxContractAudit").toString()));
        }
        if (body.containsKey("canManageFacilities")) {
            permission.setCanManageFacilities((Boolean) body.get("canManageFacilities"));
        }
        if (body.containsKey("canHandleReports")) {
            permission.setCanHandleReports((Boolean) body.get("canHandleReports"));
        }
        if (body.containsKey("canHandleViolations")) {
            permission.setCanHandleViolations((Boolean) body.get("canHandleViolations"));
        }
        if (body.containsKey("remarks")) {
            permission.setRemarks((String) body.get("remarks"));
        }
        permission.setUpdatedAt(LocalDateTime.now());

        permissionMapper.updateById(permission);
        log.info("更新权限配置: permissionId={}", permissionId);
        return Result.success("权限配置更新成功");
    }

    /**
     * 删除权限配置
     */
    @DeleteMapping("/{permissionId}")
    public Result<String> deletePermission(@PathVariable Long permissionId) {
        AdminPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return Result.error("权限配置不存在");
        }

        permissionMapper.deleteById(permissionId);
        log.info("删除权限配置: permissionId={}", permissionId);
        return Result.success("权限配置删除成功");
    }

    /**
     * 切换权限状态
     */
    @PostMapping("/{permissionId}/toggle-status")
    public Result<String> toggleStatus(@PathVariable Long permissionId) {
        AdminPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return Result.error("权限配置不存在");
        }

        String newStatus = "active".equals(permission.getStatus()) ? "inactive" : "active";
        permission.setStatus(newStatus);
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.updateById(permission);

        log.info("切换权限状态: permissionId={}, newStatus={}", permissionId, newStatus);
        return Result.success("状态已切换为" + ("active".equals(newStatus) ? "启用" : "停用"));
    }

    /**
     * 获取小区管理员列表（用于下拉选择）
     */
    @GetMapping("/admins")
    public Result<List<Map<String, Object>>> getCommunityAdmins() {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getAdminType, "community");
        List<Admin> adminList = adminMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Admin admin : adminList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", admin.getAdminId());
            item.put("adminId", admin.getAdminId());
            item.put("communityId", admin.getCommunityId());

            User user = userMapper.selectById(admin.getUserId());
            item.put("name", user != null ? user.getNickname() : "未知");

            if (admin.getCommunityId() != null) {
                Community community = communityMapper.selectById(admin.getCommunityId());
                item.put("communityName", community != null ? community.getCommunityName() : "未分配");
            } else {
                item.put("communityName", "未分配");
            }

            result.add(item);
        }

        return Result.success(result);
    }
}
