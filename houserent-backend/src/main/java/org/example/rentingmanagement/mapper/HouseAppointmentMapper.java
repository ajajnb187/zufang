package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.rentingmanagement.entity.HouseAppointment;

import java.util.List;

/**
 * 预约看房数据访问层
 */
@Mapper
public interface HouseAppointmentMapper extends BaseMapper<HouseAppointment> {

    /**
     * 查询房东的预约列表（含租客和房源信息）
     */
    @Select("SELECT ha.*, " +
            "u.nickname as user_name, u.avatar_url as user_avatar, u.phone as user_phone, " +
            "ha.tenant_id as user_id, " +
            "h.title as house_title " +
            "FROM house_appointments ha " +
            "LEFT JOIN users u ON ha.tenant_id = u.user_id " +
            "LEFT JOIN houses h ON ha.house_id = h.house_id " +
            "WHERE ha.landlord_id = #{landlordId} " +
            "ORDER BY ha.created_at DESC")
    List<HouseAppointment> findByLandlordIdWithDetails(@Param("landlordId") Long landlordId);

    /**
     * 查询租客的预约列表（含房东和房源信息）
     */
    @Select("SELECT ha.*, " +
            "u.nickname as user_name, u.avatar_url as user_avatar, u.phone as user_phone, " +
            "ha.landlord_id as user_id, " +
            "h.title as house_title " +
            "FROM house_appointments ha " +
            "LEFT JOIN users u ON ha.landlord_id = u.user_id " +
            "LEFT JOIN houses h ON ha.house_id = h.house_id " +
            "WHERE ha.tenant_id = #{tenantId} " +
            "ORDER BY ha.created_at DESC")
    List<HouseAppointment> findByTenantIdWithDetails(@Param("tenantId") Long tenantId);
}
