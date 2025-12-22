package org.example.rentingmanagement.vo;

import lombok.Data;

/**
 * 租客统计信息VO
 */
@Data
public class TenantStatisticsVO {
    
    /**
     * 总租客数
     */
    private Integer totalTenants;
    
    /**
     * 活跃租客数（当前有效合同）
     */
    private Integer activeTenants;
    
    /**
     * 即将到期租客数（30天内到期）
     */
    private Integer expiringSoon;
    
    /**
     * 新租客数（本月新签约）
     */
    private Integer newTenants;
}
