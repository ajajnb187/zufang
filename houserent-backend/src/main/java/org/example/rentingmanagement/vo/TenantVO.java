package org.example.rentingmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租客信息VO
 */
@Data
public class TenantVO {
    
    /**
     * 租客ID
     */
    private Long tenantId;
    
    /**
     * 租客姓名
     */
    private String tenantName;
    
    /**
     * 租客头像
     */
    private String avatar;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 房源标题
     */
    private String houseTitle;
    
    /**
     * 合同ID
     */
    private Long contractId;
    
    /**
     * 合同编号
     */
    private String contractNo;
    
    /**
     * 租金
     */
    private BigDecimal rentPrice;
    
    /**
     * 租赁开始日期
     */
    private LocalDate startDate;
    
    /**
     * 租赁结束日期
     */
    private LocalDate endDate;
    
    /**
     * 合同状态（原始状态：draft/signed/effective/terminated/expired）
     */
    private String contractStatus;
    
    /**
     * 审核状态（draft/pending/approved/rejected）
     */
    private String auditStatus;
    
    /**
     * 显示状态（用于前端显示：pending_sign/pending_audit/active/expiring/expired/terminated）
     */
    private String status;
    
    /**
     * 剩余天数
     */
    private Integer remainingDays;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 交易ID - 用于确认入住等操作
     */
    private Long transactionId;
    
    /**
     * 房东是否已确认入住
     */
    private Boolean landlordCheckinConfirm;
    
    /**
     * 租客是否已确认入住
     */
    private Boolean tenantCheckinConfirm;
    
    /**
     * 评分
     */
    private Double rating;
}
