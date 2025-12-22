package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.TenantListRequest;
import org.example.rentingmanagement.service.TenantService;
import org.example.rentingmanagement.service.ContractService;
import org.example.rentingmanagement.service.RevenueService;
import org.example.rentingmanagement.service.HouseService;
import org.example.rentingmanagement.vo.TenantStatisticsVO;
import org.example.rentingmanagement.vo.TenantVO;
import org.example.rentingmanagement.vo.RevenueDetailVO;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房东管理控制器
 */
@RestController
@RequestMapping("/api/landlord")
@RequiredArgsConstructor
@SaCheckRole("landlord")
public class LandlordController {

    private final TenantService tenantService;
    private final ContractService contractService;
    private final RevenueService revenueService;
    private final HouseService houseService;

    /**
     * 获取租客列表
     */
    @GetMapping("/tenants")
    public Result<IPage<TenantVO>> getTenantList(
            @RequestParam(required = false) Long houseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        
        TenantListRequest request = new TenantListRequest();
        request.setHouseId(houseId);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        
        IPage<TenantVO> tenantList = tenantService.getTenantList(landlordId, request);
        return Result.success(tenantList);
    }

    /**
     * 获取租客详细信息
     */
    @GetMapping("/tenants/{tenantId}")
    public Result<TenantVO> getTenantDetail(@PathVariable Long tenantId) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        TenantVO tenantDetail = tenantService.getTenantDetail(landlordId, tenantId);
        if (tenantDetail == null) {
            return Result.error("租客不存在或无权限查看");
        }
        return Result.success(tenantDetail);
    }

    /**
     * 获取租客统计信息
     */
    @GetMapping("/tenants/statistics")
    public Result<TenantStatisticsVO> getTenantStats() {
        Long landlordId = StpUtil.getLoginIdAsLong();
        TenantStatisticsVO statistics = tenantService.getTenantStatistics(landlordId);
        return Result.success(statistics);
    }

    /**
     * 获取收益统计
     */
    @GetMapping("/revenue/statistics")
    public Result<Map<String, Object>> getRevenueStatistics(
            @RequestParam(required = false) String period) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        Map<String, Object> statistics = revenueService.getLandlordRevenueStatistics(landlordId, period);
        return Result.success(statistics);
    }

    /**
     * 获取收益明细列表
     */
    @GetMapping("/revenue/details")
    public Result<IPage<RevenueDetailVO>> getRevenueDetails(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        IPage<RevenueDetailVO> revenueDetails = revenueService.getLandlordRevenueDetails(landlordId, startDate, endDate, pageNum, pageSize);
        return Result.success(revenueDetails);
    }

    /**
     * 获取房源收益排行
     */
    @GetMapping("/revenue/ranking")
    public Result<List<Map<String, Object>>> getHouseRevenueRanking() {
        Long landlordId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> ranking = revenueService.getHouseRevenueRanking(landlordId);
        return Result.success(ranking);
    }

    /**
     * 获取合同统计信息
     */
    @GetMapping("/contracts/statistics")
    public Result<Map<String, Object>> getContractStats() {
        Long landlordId = StpUtil.getLoginIdAsLong();
        Map<String, Object> statistics = contractService.getLandlordContractStatistics(landlordId);
        return Result.success(statistics);
    }

    /**
     * 获取房东的合同列表
     */
    @GetMapping("/contracts")
    public Result<IPage<org.example.rentingmanagement.entity.RentalContract>> getLandlordContracts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        IPage<org.example.rentingmanagement.entity.RentalContract> contracts = contractService.getLandlordContracts(landlordId, status, pageNum, pageSize);
        return Result.success(contracts);
    }

    /**
     * 获取房东的房源统计
     */
    @GetMapping("/houses/statistics")
    public Result<Map<String, Object>> getHouseStatistics() {
        Long landlordId = StpUtil.getLoginIdAsLong();
        Map<String, Object> statistics = houseService.getLandlordHouseStatistics(landlordId);
        return Result.success(statistics);
    }
}
