package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 管理员数据访问层
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户ID查询管理员信息
     */
    @Select("SELECT * FROM admins WHERE user_id = #{userId} AND status = 'active'")
    Admin findByUserId(Long userId);

    /**
     * 查询小区管理员列表
     */
    @Select("SELECT * FROM admins WHERE admin_type = 'community' AND status = 'active'")
    List<Admin> findCommunityAdmins();

    /**
     * 查询平台管理员列表
     */
    @Select("SELECT * FROM admins WHERE admin_type = 'platform' AND status = 'active'")
    List<Admin> findPlatformAdmins();

    /**
     * 根据小区ID查询小区管理员
     */
    @Select("SELECT * FROM admins WHERE community_id = #{communityId} AND admin_type = 'community' AND status = 'active'")
    List<Admin> findByCommunityId(Long communityId);
}
