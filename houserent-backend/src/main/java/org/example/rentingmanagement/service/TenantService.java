package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.dto.TenantListRequest;
import org.example.rentingmanagement.vo.TenantStatisticsVO;
import org.example.rentingmanagement.vo.TenantVO;

import java.util.Map;

/**
 * 租客管理服务接口
 */
public interface TenantService {

    /**
     * 获取房东的租客统计信息
     */
    TenantStatisticsVO getTenantStatistics(Long landlordId);

    /**
     * 获取房东的租客列表
     */
    IPage<TenantVO> getTenantList(Long landlordId, TenantListRequest request);

    /**
     * 获取租客详细信息
     */
    TenantVO getTenantDetail(Long landlordId, Long tenantId);
}
