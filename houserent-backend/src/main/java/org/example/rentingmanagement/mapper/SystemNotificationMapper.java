package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.SystemNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 系统通知数据访问层
 */
@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    /**
     * 查询用户通知列表
     */
    @Select("SELECT * FROM system_notifications WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemNotification> findByUserId(Long userId, Integer limit);

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM system_notifications WHERE user_id = #{userId} AND is_read = 0")
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    @Update("UPDATE system_notifications SET is_read = 1, read_time = NOW() WHERE notification_id = #{notificationId}")
    void markAsRead(Long notificationId);

    /**
     * 标记用户所有通知为已读
     */
    @Update("UPDATE system_notifications SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    void markAllAsRead(Long userId);
}
