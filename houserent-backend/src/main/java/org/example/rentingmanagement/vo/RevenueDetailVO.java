package org.example.rentingmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收益明细VO
 */
@Data
public class RevenueDetailVO {
    
    /**
     * 收益ID
     */
    private Long revenueId;
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 房源标题
     */
    private String houseTitle;
    
    /**
     * 租客ID
     */
    private Long tenantId;
    
    /**
     * 租客姓名
     */
    private String tenantName;
    
    /**
     * 收益金额
     */
    private BigDecimal amount;
    
    /**
     * 收益类型（租金、押金等）
     */
    private String revenueType;
    
    /**
     * 收益日期
     */
    private LocalDate revenueDate;
    
    /**
     * 支付状态
     */
    private String paymentStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
