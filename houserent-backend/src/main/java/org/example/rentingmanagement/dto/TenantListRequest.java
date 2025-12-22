package org.example.rentingmanagement.dto;

import lombok.Data;

/**
 * 租客列表查询请求DTO
 */
@Data
public class TenantListRequest {
    
    /**
     * 房源ID（可选，筛选特定房源的租客）
     */
    private Long houseId;
    
    /**
     * 合同状态（可选）
     */
    private String status;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 搜索关键词（租客姓名或电话）
     */
    private String keyword;
}
