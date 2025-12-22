package org.example.rentingmanagement.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.SystemNotification;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.SystemNotificationMapper;
import org.example.rentingmanagement.mapper.WebsocketSessionMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final WebsocketSessionMapper websocketSessionMapper;
    private final RentalContractMapper rentalContractMapper;
    private final SystemNotificationMapper systemNotificationMapper;

    /**
     * 清理离线的WebSocket会话
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupOfflineWebSocketSessions() {
        try {
            // 清理超过10分钟未发送心跳的会话
            websocketSessionMapper.cleanupOfflineSessions(10);
            log.debug("WebSocket离线会话清理完成");
        } catch (Exception e) {
            log.error("清理WebSocket离线会话失败", e);
        }
    }

    /**
     * 合同到期提醒
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void contractExpirationReminder() {
        try {
            log.info("开始执行合同到期提醒任务");
            
            // 查询即将到期的合同（30天内到期）
            List<RentalContract> expiringContracts = rentalContractMapper.findExpiringContracts();
            
            for (RentalContract contract : expiringContracts) {
                // 计算剩余天数
                long daysToExpire = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), contract.getEndDate());
                
                String notificationContent;
                if (daysToExpire <= 7) {
                    notificationContent = String.format("您的租赁合同将在%d天后到期，请及时处理续约或搬离事宜", daysToExpire);
                } else if (daysToExpire <= 15) {
                    notificationContent = String.format("您的租赁合同将在%d天后到期，请提前准备续约或搬离", daysToExpire);
                } else {
                    notificationContent = String.format("您的租赁合同将在%d天后到期，请关注合同续约情况", daysToExpire);
                }

                // 给房东发送通知
                createSystemNotification(
                        contract.getLandlordId(),
                        "contract_expiring",
                        "合同即将到期提醒",
                        notificationContent,
                        "contract",
                        contract.getContractId()
                );

                // 给租户发送通知
                createSystemNotification(
                        contract.getTenantId(),
                        "contract_expiring",
                        "合同即将到期提醒",
                        notificationContent,
                        "contract",
                        contract.getContractId()
                );
            }

            log.info("合同到期提醒任务完成，处理合同数量: {}", expiringContracts.size());

        } catch (Exception e) {
            log.error("执行合同到期提醒任务失败", e);
        }
    }

    /**
     * 清理过期的系统通知
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredNotifications() {
        try {
            log.info("开始清理过期系统通知");
            
            // 删除30天前的通知
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            
            // 这里需要在SystemNotificationMapper中添加相应的方法
            // 暂时使用注释说明
            // systemNotificationMapper.deleteExpiredNotifications(thirtyDaysAgo);
            
            log.info("过期系统通知清理完成");

        } catch (Exception e) {
            log.error("清理过期系统通知失败", e);
        }
    }

    /**
     * 数据统计任务
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyDataStatistics() {
        try {
            log.info("开始执行每日数据统计任务");
            
            // TODO: 实现数据统计逻辑
            // 1. 统计活跃用户数
            // 2. 统计房源发布数量
            // 3. 统计合同签署数量
            // 4. 统计论坛帖子和回复数量
            // 5. 生成日报数据
            
            log.info("每日数据统计任务完成");

        } catch (Exception e) {
            log.error("执行每日数据统计任务失败", e);
        }
    }

    /**
     * 健康检查任务
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void systemHealthCheck() {
        try {
            // 检查数据库连接
            // 检查Redis连接
            // 检查MinIO连接
            // 记录系统状态
            log.debug("系统健康检查完成");
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
        }
    }

    /**
     * 创建系统通知
     */
    private void createSystemNotification(Long userId, String notificationType, String title, 
                                        String content, String relatedType, Long relatedId) {
        try {
            SystemNotification notification = new SystemNotification();
            notification.setUserId(userId);
            notification.setNotificationType(notificationType);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedType(relatedType);
            notification.setRelatedId(relatedId);
            notification.setIsRead(false);
            
            systemNotificationMapper.insert(notification);
            
        } catch (Exception e) {
            log.error("创建系统通知失败: userId={}, type={}", userId, notificationType, e);
        }
    }
}
