package org.example.rentingmanagement.service;

import java.util.Map;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.vo.RevenueDetailVO;

/**
 * 收益管理服务接口
 */
public interface RevenueService {

    /**
     * 获取房东收益统计
     */
    Map<String, Object> getLandlordRevenueStatistics(Long landlordId, String period);

    /**
     * 获取房东收益明细
     */
    IPage<RevenueDetailVO> getLandlordRevenueDetails(Long landlordId, String startDate, String endDate, Integer pageNum, Integer pageSize);

    /**
     * 获取房源收益排行
     */
    List<Map<String, Object>> getHouseRevenueRanking(Long landlordId);
}
