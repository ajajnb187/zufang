package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.entity.*;
import org.example.rentingmanagement.mapper.*;
import org.example.rentingmanagement.service.SystemNotificationService;
import org.example.rentingmanagement.service.WebSocketChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification> implements SystemNotificationService {

    private final RentalContractMapper rentalContractMapper;
    private final HouseMapper houseMapper;
    private final CommunityVerificationMapper communityVerificationMapper;
    private final ReportMapper reportMapper;
    private final FacilityFeedbackMapper facilityFeedbackMapper;
    private final CommunityFacilityMapper communityFacilityMapper;
    private final CommunityMapper communityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemNotification createNotification(Long userId, String notificationType, String title, 
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

            this.save(notification);

            // 尝试通过WebSocket实时推送通知
            try {
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("type", "notification");
                notificationData.put("notificationType", notificationType);
                notificationData.put("title", title);
                notificationData.put("content", content);
                notificationData.put("notificationId", notification.getNotificationId());

                String message = com.alibaba.fastjson2.JSON.toJSONString(notificationData);
                WebSocketChatService.sendMessageToUser(userId, message);
            } catch (Exception e) {
                log.warn("WebSocket推送通知失败: userId={}, notificationType={}", userId, notificationType, e);
            }

            log.info("系统通知创建成功: userId={}, type={}, title={}", userId, notificationType, title);
            return notification;

        } catch (Exception e) {
            log.error("创建系统通知失败: userId={}, type={}", userId, notificationType, e);
            throw new BusinessException("创建通知失败");
        }
    }

    @Override
    public IPage<SystemNotification> getUserNotifications(Long userId, Integer pageNum, Integer pageSize) {
        try {
            Page<SystemNotification> page = new Page<>(pageNum, pageSize);
            List<SystemNotification> notifications = baseMapper.findByUserId(userId, pageSize);
            return page.setRecords(notifications);
        } catch (Exception e) {
            log.error("获取用户通知列表失败: userId={}", userId, e);
            throw new BusinessException("获取通知列表失败");
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        try {
            return baseMapper.getUnreadCount(userId);
        } catch (Exception e) {
            log.error("获取未读通知数量失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        try {
            SystemNotification notification = this.getById(notificationId);
            if (notification == null) {
                throw new BusinessException("通知不存在");
            }

            if (!notification.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此通知");
            }

            if (!notification.getIsRead()) {
                baseMapper.markAsRead(notificationId);
                log.info("通知标记为已读: notificationId={}, userId={}", notificationId, userId);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("标记通知已读失败: notificationId={}, userId={}", notificationId, userId, e);
            throw new BusinessException("标记通知失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        try {
            baseMapper.markAllAsRead(userId);
            log.info("所有通知标记为已读: userId={}", userId);
        } catch (Exception e) {
            log.error("标记所有通知已读失败: userId={}", userId, e);
            throw new BusinessException("标记通知失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            SystemNotification notification = this.getById(notificationId);
            if (notification == null) {
                throw new BusinessException("通知不存在");
            }

            if (!notification.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此通知");
            }

            this.removeById(notificationId);
            log.info("通知删除成功: notificationId={}, userId={}", notificationId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除通知失败: notificationId={}, userId={}", notificationId, userId, e);
            throw new BusinessException("删除通知失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSendNotifications(List<Long> userIds, String notificationType, 
                                     String title, String content, String relatedType, Long relatedId) {
        try {
            for (Long userId : userIds) {
                createNotification(userId, notificationType, title, content, relatedType, relatedId);
            }
            log.info("批量发送通知完成: 用户数量={}, type={}", userIds.size(), notificationType);
        } catch (Exception e) {
            log.error("批量发送通知失败: type={}", notificationType, e);
            throw new BusinessException("批量发送通知失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendContractNotification(Long contractId, String notificationType, String customMessage) {
        try {
            RentalContract contract = rentalContractMapper.selectById(contractId);
            if (contract == null) {
                log.warn("合同不存在，无法发送通知: contractId={}", contractId);
                return;
            }

            String title = getContractNotificationTitle(notificationType);
            String content = customMessage != null ? customMessage : getContractNotificationContent(notificationType, contract);

            // 给房东发送通知
            createNotification(contract.getLandlordId(), notificationType, title, content, "contract", contractId);

            // 给租户发送通知
            createNotification(contract.getTenantId(), notificationType, title, content, "contract", contractId);

            log.info("合同通知发送成功: contractId={}, type={}", contractId, notificationType);

        } catch (Exception e) {
            log.error("发送合同通知失败: contractId={}, type={}", contractId, notificationType, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendHouseNotification(Long houseId, String notificationType, String customMessage) {
        try {
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                log.warn("房源不存在，无法发送通知: houseId={}", houseId);
                return;
            }

            String title = getHouseNotificationTitle(notificationType);
            String content = customMessage != null ? customMessage : getHouseNotificationContent(notificationType, house);

            // 给房东发送通知
            createNotification(house.getLandlordId(), notificationType, title, content, "house", houseId);

            log.info("房源通知发送成功: houseId={}, type={}", houseId, notificationType);

        } catch (Exception e) {
            log.error("发送房源通知失败: houseId={}, type={}", houseId, notificationType, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendVerificationNotification(Long verificationId, String notificationType, String result) {
        try {
            CommunityVerification verification = communityVerificationMapper.selectById(verificationId);
            if (verification == null) {
                log.warn("认证申请不存在，无法发送通知: verificationId={}", verificationId);
                return;
            }

            String title = getVerificationNotificationTitle(notificationType);
            String content = getVerificationNotificationContent(notificationType, result);

            // 给申请人发送通知
            createNotification(verification.getUserId(), notificationType, title, content, "verification", verificationId);

            log.info("认证通知发送成功: verificationId={}, type={}, result={}", verificationId, notificationType, result);

        } catch (Exception e) {
            log.error("发送认证通知失败: verificationId={}, type={}", verificationId, notificationType, e);
        }
    }

    /**
     * 获取合同通知标题
     */
    private String getContractNotificationTitle(String notificationType) {
        switch (notificationType) {
            case "contract_created":
                return "合同创建通知";
            case "contract_signed":
                return "合同签署通知";
            case "contract_approved":
                return "合同审核通过";
            case "contract_rejected":
                return "合同审核被拒";
            case "contract_expiring":
                return "合同即将到期";
            case "contract_terminated":
                return "合同终止通知";
            default:
                return "合同通知";
        }
    }

    /**
     * 获取合同通知内容
     */
    private String getContractNotificationContent(String notificationType, RentalContract contract) {
        switch (notificationType) {
            case "contract_created":
                return "您的租赁合同已创建，合同编号：" + contract.getContractNo() + "，请及时查看并签署";
            case "contract_signed":
                return "租赁合同已完成双方签署，合同编号：" + contract.getContractNo() + "，等待管理员审核";
            case "contract_approved":
                return "您的租赁合同已通过审核，合同编号：" + contract.getContractNo() + "，合同正式生效";
            case "contract_rejected":
                return "您的租赁合同审核未通过，合同编号：" + contract.getContractNo() + "，请查看审核意见";
            case "contract_terminated":
                return "租赁合同已终止，合同编号：" + contract.getContractNo() + "，请及时处理相关事宜";
            default:
                return "您有新的合同通知，请及时查看";
        }
    }

    /**
     * 获取房源通知标题
     */
    private String getHouseNotificationTitle(String notificationType) {
        switch (notificationType) {
            case "house_approved":
                return "房源审核通过";
            case "house_rejected":
                return "房源审核被拒";
            case "house_inquiry":
                return "房源咨询";
            case "house_favorite":
                return "房源收藏";
            default:
                return "房源通知";
        }
    }

    /**
     * 获取房源通知内容
     */
    private String getHouseNotificationContent(String notificationType, House house) {
        switch (notificationType) {
            case "house_approved":
                return "您发布的房源\"" + house.getTitle() + "\"已通过审核，现已上线展示";
            case "house_rejected":
                return "您发布的房源\"" + house.getTitle() + "\"审核未通过，请查看审核意见并修改";
            case "house_inquiry":
                return "有用户对您的房源\"" + house.getTitle() + "\"感兴趣，请及时回复咨询";
            case "house_favorite":
                return "有用户收藏了您的房源\"" + house.getTitle() + "\"";
            default:
                return "您有新的房源相关通知";
        }
    }

    /**
     * 获取认证通知标题
     */
    private String getVerificationNotificationTitle(String notificationType) {
        switch (notificationType) {
            case "verification_approved":
                return "小区认证通过";
            case "verification_rejected":
                return "小区认证被拒";
            case "verification_community_approved":
                return "小区管理员审核通过";
            case "verification_community_rejected":
                return "小区管理员审核被拒";
            default:
                return "认证通知";
        }
    }

    /**
     * 获取认证通知内容
     */
    private String getVerificationNotificationContent(String notificationType, String result) {
        switch (notificationType) {
            case "verification_approved":
                return "恭喜！您的小区身份认证已通过审核，现在可以在小区内发布房源和参与论坛讨论";
            case "verification_rejected":
                return "很抱歉，您的小区身份认证未通过审核。" + (result != null ? "原因：" + result : "请重新提交认证材料");
            case "verification_community_approved":
                return "您的小区身份认证已通过小区管理员审核，正在等待平台最终审核";
            case "verification_community_rejected":
                return "您的小区身份认证未通过小区管理员审核。" + (result != null ? "原因：" + result : "请重新提交认证材料");
            default:
                return "您有新的认证相关通知";
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendReportNotification(Long reportId, String notificationType, String result) {
        try {
            // 查询举报记录
            Report report = reportMapper.selectById(reportId);
            if (report == null) {
                log.warn("举报记录不存在，无法发送通知: reportId={}", reportId);
                return;
            }
            
            // 获取举报的房源信息
            String houseTitle = "";
            if ("house".equals(report.getReportType())) {
                House house = houseMapper.selectById(report.getTargetId());
                if (house != null) {
                    houseTitle = house.getTitle();
                }
            }
            
            // 根据通知类型生成标题和内容
            String title = getReportNotificationTitle(notificationType);
            String content = getReportNotificationContent(notificationType, report.getReasonType(), 
                    report.getReasonDetail(), houseTitle, result);
            
            // 给举报人发送通知
            createNotification(report.getReporterId(), notificationType, title, content, "report", reportId);
            
            log.info("举报通知发送成功: reportId={}, reporterId={}, type={}", reportId, report.getReporterId(), notificationType);
            
        } catch (Exception e) {
            log.error("发送举报通知失败: reportId={}, type={}", reportId, notificationType, e);
        }
    }
    
    /**
     * 获取举报通知标题
     */
    private String getReportNotificationTitle(String notificationType) {
        switch (notificationType) {
            case "report_handled":
                return "举报处理结果通知";
            case "report_rejected":
                return "举报审核结果通知";
            case "report_received":
                return "举报已受理通知";
            default:
                return "举报相关通知";
        }
    }
    
    /**
     * 获取举报通知内容
     */
    private String getReportNotificationContent(String notificationType, String reasonType, 
            String reasonDetail, String houseTitle, String result) {
        String houseInfo = houseTitle != null && !houseTitle.isEmpty() ? "【" + houseTitle + "】" : "";
        String reasonTypeName = getReasonTypeName(reasonType);
        String detailBrief = reasonDetail != null && reasonDetail.length() > 30 ? 
                reasonDetail.substring(0, 30) + "..." : reasonDetail;
        
        switch (notificationType) {
            case "report_handled":
                return "您举报的房源" + houseInfo + "（举报类型：" + reasonTypeName + 
                       (detailBrief != null && !detailBrief.isEmpty() ? "，举报内容：" + detailBrief : "") + 
                       "）已处理完成。" + 
                       (result != null && !result.isEmpty() ? "处理结果：" + result : "感谢您的反馈，我们会持续改进平台服务。");
            case "report_rejected":
                return "您举报的房源" + houseInfo + "（举报类型：" + reasonTypeName + 
                       (detailBrief != null && !detailBrief.isEmpty() ? "，举报内容：" + detailBrief : "") + 
                       "）经审核后未被采纳。" + 
                       (result != null && !result.isEmpty() ? "原因：" + result : "如有疑问请联系客服。");
            case "report_received":
                return "您举报的房源" + houseInfo + "已受理，我们会尽快处理并通知您结果。";
            default:
                return "您有新的举报相关通知，请查看详情。";
        }
    }
    
    /**
     * 获取举报类型中文名称
     */
    private String getReasonTypeName(String reasonType) {
        if (reasonType == null) return "其他";
        switch (reasonType) {
            case "false_info": return "虚假信息";
            case "fraud": return "涉嫌欺诈";
            case "illegal": return "违法违规";
            case "duplicate": return "重复发布";
            case "inappropriate": return "内容不当";
            case "price_fraud": return "价格欺诈";
            default: return reasonType;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendFacilityFeedbackNotification(Long feedbackId, String notificationType, String handlerReply) {
        try {
            // 查询反馈记录
            FacilityFeedback feedback = facilityFeedbackMapper.selectById(feedbackId);
            if (feedback == null) {
                log.warn("配套反馈记录不存在，无法发送通知: feedbackId={}", feedbackId);
                return;
            }
            
            // 获取小区名称
            String communityName = "";
            Community community = communityMapper.selectById(feedback.getCommunityId());
            if (community != null) {
                communityName = community.getCommunityName();
            }
            
            // 获取配套设施名称
            String facilityName = "";
            if (feedback.getFacilityId() != null) {
                CommunityFacility facility = communityFacilityMapper.selectById(feedback.getFacilityId());
                if (facility != null) {
                    facilityName = facility.getName();
                }
            }
            
            // 根据通知类型生成标题和内容
            String title = getFeedbackNotificationTitle(notificationType);
            String content = getFeedbackNotificationContent(notificationType, feedback.getContent(), 
                    communityName, facilityName, handlerReply);
            
            // 给反馈用户发送通知
            createNotification(feedback.getUserId(), notificationType, title, content, "facility_feedback", feedbackId);
            
            log.info("配套反馈通知发送成功: feedbackId={}, userId={}, type={}", feedbackId, feedback.getUserId(), notificationType);
            
        } catch (Exception e) {
            log.error("发送配套反馈通知失败: feedbackId={}, type={}", feedbackId, notificationType, e);
        }
    }
    
    /**
     * 获取配套反馈通知标题
     */
    private String getFeedbackNotificationTitle(String notificationType) {
        switch (notificationType) {
            case "feedback_processed":
                return "配套反馈已处理";
            case "feedback_rejected":
                return "配套反馈未采纳";
            case "feedback_received":
                return "配套反馈已收到";
            default:
                return "配套反馈通知";
        }
    }
    
    /**
     * 获取配套反馈通知内容
     */
    private String getFeedbackNotificationContent(String notificationType, String userContent, 
            String communityName, String facilityName, String handlerReply) {
        String facilityInfo = facilityName != null && !facilityName.isEmpty() ? "【" + facilityName + "】" : "";
        String communityInfo = communityName != null && !communityName.isEmpty() ? communityName : "您所在小区";
        String feedbackBrief = userContent != null && userContent.length() > 20 ? 
                userContent.substring(0, 20) + "..." : userContent;
        
        switch (notificationType) {
            case "feedback_processed":
                return "您在" + communityInfo + "提交的配套信息反馈" + facilityInfo + 
                       "（反馈内容：" + feedbackBrief + "）已处理完成。" + 
                       (handlerReply != null && !handlerReply.isEmpty() ? "处理回复：" + handlerReply : "感谢您的反馈！");
            case "feedback_rejected":
                return "您在" + communityInfo + "提交的配套信息反馈" + facilityInfo + 
                       "（反馈内容：" + feedbackBrief + "）经审核后未被采纳。" + 
                       (handlerReply != null && !handlerReply.isEmpty() ? "原因：" + handlerReply : "如有疑问请联系管理员。");
            case "feedback_received":
                return "您在" + communityInfo + "提交的配套信息反馈" + facilityInfo + "已收到，我们会尽快处理。";
            default:
                return "您有新的配套反馈相关通知，请查看详情。";
        }
    }
}
