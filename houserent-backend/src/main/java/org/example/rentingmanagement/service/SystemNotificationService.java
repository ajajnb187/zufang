package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.entity.SystemNotification;

/**
 * 系统通知服务接口
 */
public interface SystemNotificationService {

    /**
     * 创建系统通知
     */
    SystemNotification createNotification(Long userId, String notificationType, String title, 
                                        String content, String relatedType, Long relatedId);

    /**
     * 获取用户通知列表
     */
    IPage<SystemNotification> getUserNotifications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 批量发送通知
     */
    void batchSendNotifications(java.util.List<Long> userIds, String notificationType, 
                               String title, String content, String relatedType, Long relatedId);

    /**
     * 发送合同相关通知
     */
    void sendContractNotification(Long contractId, String notificationType, String customMessage);

    /**
     * 发送房源相关通知
     */
    void sendHouseNotification(Long houseId, String notificationType, String customMessage);

    /**
     * 发送小区认证通知
     */
    void sendVerificationNotification(Long verificationId, String notificationType, String result);
    
    /**
     * 发送举报处理通知
     */
    void sendReportNotification(Long reportId, String notificationType, String result);
    
    /**
     * 发送配套反馈处理通知
     */
    void sendFacilityFeedbackNotification(Long feedbackId, String notificationType, String handlerReply);
}
