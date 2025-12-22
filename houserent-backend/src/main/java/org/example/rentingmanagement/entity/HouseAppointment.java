package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约看房实体类
 */
@Data
@TableName("house_appointments")
public class HouseAppointment {

    @TableId(value = "appointment_id", type = IdType.AUTO)
    private Long appointmentId;

    /**
     * 房源ID
     */
    @TableField("house_id")
    private Long houseId;

    /**
     * 租客用户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 房东用户ID
     */
    @TableField("landlord_id")
    private Long landlordId;

    /**
     * 预约时间
     */
    @TableField("appointment_time")
    private LocalDateTime appointmentTime;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 留言备注
     */
    @TableField("message")
    private String message;

    /**
     * 预约状态: pending-待确认, confirmed-已确认, cancelled-已取消, completed-已完成
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // ========== 非数据库字段，用于前端展示 ==========
    
    /**
     * 租客姓名
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 租客头像
     */
    @TableField(exist = false)
    private String userAvatar;

    /**
     * 租客电话
     */
    @TableField(exist = false)
    private String userPhone;

    /**
     * 房源标题
     */
    @TableField(exist = false)
    private String houseTitle;

    /**
     * 租客用户ID（别名）
     */
    @TableField(exist = false)
    private Long userId;
}
