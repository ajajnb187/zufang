package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.rentingmanagement.entity.AdminPermission;

/**
 * 管理员权限配置Mapper接口
 */
@Mapper
public interface AdminPermissionMapper extends BaseMapper<AdminPermission> {

    @Select("SELECT * FROM admin_permissions WHERE admin_id = #{adminId}")
    AdminPermission findByAdminId(@Param("adminId") Long adminId);

    @Select("SELECT COUNT(*) FROM admin_permissions WHERE admin_id = #{adminId} AND status = 'active'")
    int checkAdminActive(@Param("adminId") Long adminId);
}