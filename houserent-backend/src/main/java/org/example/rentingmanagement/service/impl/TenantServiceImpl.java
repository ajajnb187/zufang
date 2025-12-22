package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.dto.TenantListRequest;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.RentalTransactionMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.mapper.HouseMapper;
import org.example.rentingmanagement.entity.RentalTransaction;
import org.example.rentingmanagement.service.TenantService;
import org.example.rentingmanagement.vo.TenantStatisticsVO;
import org.example.rentingmanagement.vo.TenantVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租客管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final RentalContractMapper contractMapper;
    private final RentalTransactionMapper transactionMapper;
    private final UserMapper userMapper;
    private final HouseMapper houseMapper;

    @Override
    public TenantStatisticsVO getTenantStatistics(Long landlordId) {
        try {
            TenantStatisticsVO statistics = new TenantStatisticsVO();
            
            // 总租客数（所有合同的租客，去重）
            QueryWrapper<RentalContract> totalQuery = new QueryWrapper<>();
            totalQuery.eq("landlord_id", landlordId);
            List<RentalContract> allContracts = contractMapper.selectList(totalQuery);
            long totalTenants = allContracts.stream()
                    .map(RentalContract::getTenantId)
                    .distinct()
                    .count();
            statistics.setTotalTenants((int) totalTenants);
            
            // 活跃租客数（当前有效合同）
            QueryWrapper<RentalContract> activeQuery = new QueryWrapper<>();
            activeQuery.eq("landlord_id", landlordId)
                      .eq("contract_status", "effective")
                      .ge("end_date", LocalDate.now());
            long activeTenants = contractMapper.selectCount(activeQuery);
            statistics.setActiveTenants((int) activeTenants);
            
            // 即将到期租客数（30天内到期）
            LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
            QueryWrapper<RentalContract> expiringQuery = new QueryWrapper<>();
            expiringQuery.eq("landlord_id", landlordId)
                        .eq("contract_status", "effective")
                        .between("end_date", LocalDate.now(), thirtyDaysLater);
            long expiringSoon = contractMapper.selectCount(expiringQuery);
            statistics.setExpiringSoon((int) expiringSoon);
            
            // 新租客数（本月新签约）
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            QueryWrapper<RentalContract> newQuery = new QueryWrapper<>();
            newQuery.eq("landlord_id", landlordId)
                   .ge("created_at", monthStart);
            long newTenants = contractMapper.selectCount(newQuery);
            statistics.setNewTenants((int) newTenants);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取租客统计信息失败: landlordId={}", landlordId, e);
            // 返回默认值
            TenantStatisticsVO defaultStats = new TenantStatisticsVO();
            defaultStats.setTotalTenants(0);
            defaultStats.setActiveTenants(0);
            defaultStats.setExpiringSoon(0);
            defaultStats.setNewTenants(0);
            return defaultStats;
        }
    }

    @Override
    public IPage<TenantVO> getTenantList(Long landlordId, TenantListRequest request) {
        try {
            Page<RentalContract> page = new Page<>(request.getPageNum(), request.getPageSize());
            
            QueryWrapper<RentalContract> query = new QueryWrapper<>();
            query.eq("landlord_id", landlordId);
            
            // 按房源筛选
            if (request.getHouseId() != null) {
                query.eq("house_id", request.getHouseId());
            }
            
            // 按合同状态筛选
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                query.eq("contract_status", request.getStatus());
            }
            
            // 按创建时间倒序
            query.orderByDesc("created_at");
            
            IPage<RentalContract> contractPage = contractMapper.selectPage(page, query);
            
            // 转换为TenantVO
            List<TenantVO> tenantList = contractPage.getRecords().stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            // 如果有关键词搜索，进行二次过滤
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = request.getKeyword().trim().toLowerCase();
                tenantList = tenantList.stream()
                        .filter(tenant -> 
                            (tenant.getTenantName() != null && tenant.getTenantName().toLowerCase().contains(keyword)) ||
                            (tenant.getPhone() != null && tenant.getPhone().contains(keyword))
                        )
                        .collect(Collectors.toList());
            }
            
            Page<TenantVO> resultPage = new Page<>(request.getPageNum(), request.getPageSize());
            resultPage.setRecords(tenantList);
            resultPage.setTotal(contractPage.getTotal());
            
            return resultPage;
            
        } catch (Exception e) {
            log.error("获取租客列表失败: landlordId={}", landlordId, e);
            return new Page<>(request.getPageNum(), request.getPageSize());
        }
    }

    @Override
    public TenantVO getTenantDetail(Long landlordId, Long tenantId) {
        try {
            // 查找该房东和租客的最新合同
            QueryWrapper<RentalContract> query = new QueryWrapper<>();
            query.eq("landlord_id", landlordId)
                 .eq("tenant_id", tenantId)
                 .orderByDesc("created_at")
                 .last("LIMIT 1");
            
            RentalContract contract = contractMapper.selectOne(query);
            if (contract == null) {
                return null;
            }
            
            return convertToTenantVO(contract);
            
        } catch (Exception e) {
            log.error("获取租客详情失败: landlordId={}, tenantId={}", landlordId, tenantId, e);
            return null;
        }
    }

    /**
     * 转换合同为租客VO
     */
    private TenantVO convertToTenantVO(RentalContract contract) {
        TenantVO tenantVO = new TenantVO();
        
        try {
            // 基本合同信息
            tenantVO.setContractId(contract.getContractId());
            tenantVO.setContractNo(contract.getContractNo());
            tenantVO.setRentPrice(contract.getRentPrice());
            tenantVO.setStartDate(contract.getStartDate());
            tenantVO.setEndDate(contract.getEndDate());
            tenantVO.setContractStatus(contract.getContractStatus());
            tenantVO.setAuditStatus(contract.getAuditStatus());
            tenantVO.setCreatedAt(contract.getCreatedAt());
            
            // 计算剩余天数
            int remainingDays = 0;
            if (contract.getEndDate() != null) {
                remainingDays = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), contract.getEndDate()));
                tenantVO.setRemainingDays(remainingDays);
            }
            
            // 计算显示状态
            String displayStatus = calculateDisplayStatus(contract.getContractStatus(), contract.getAuditStatus(), remainingDays);
            tenantVO.setStatus(displayStatus);
            
            // 获取租客信息
            User tenant = userMapper.selectById(contract.getTenantId());
            if (tenant != null) {
                tenantVO.setTenantId(tenant.getUserId());
                tenantVO.setTenantName(tenant.getNickname());
                tenantVO.setAvatar(tenant.getAvatarUrl());
                tenantVO.setPhone(tenant.getPhone());
            }
            
            // 获取房源信息
            House house = houseMapper.selectById(contract.getHouseId());
            if (house != null) {
                tenantVO.setHouseId(house.getHouseId());
                tenantVO.setHouseTitle(house.getTitle());
            }
            
            // 获取交易信息（用于入住确认等操作）
            RentalTransaction transaction = transactionMapper.findByContractId(contract.getContractId());
            if (transaction != null) {
                tenantVO.setTransactionId(transaction.getTransactionId());
                tenantVO.setLandlordCheckinConfirm(transaction.getLandlordCheckinConfirm());
                tenantVO.setTenantCheckinConfirm(transaction.getTenantCheckinConfirm());
            }
            
        } catch (Exception e) {
            log.error("转换租客VO失败: contractId={}", contract.getContractId(), e);
        }
        
        return tenantVO;
    }
    
    /**
     * 计算显示状态
     * @param contractStatus 合同状态（draft/signed/effective/terminated/expired）
     * @param auditStatus 审核状态（draft/pending/approved/rejected）
     * @param remainingDays 剩余天数
     * @return 显示状态
     */
    private String calculateDisplayStatus(String contractStatus, String auditStatus, int remainingDays) {
        // 草稿状态 - 待签署
        if ("draft".equals(contractStatus)) {
            return "pending_sign";
        }
        
        // 已签署但待审核
        if ("signed".equals(contractStatus) && "pending".equals(auditStatus)) {
            return "pending_audit";
        }
        
        // 生效中的合同
        if ("effective".equals(contractStatus)) {
            // 即将到期（30天内）
            if (remainingDays <= 30 && remainingDays > 0) {
                return "expiring";
            }
            // 已过期
            if (remainingDays <= 0) {
                return "expired";
            }
            // 正常在租
            return "active";
        }
        
        // 已终止
        if ("terminated".equals(contractStatus)) {
            return "terminated";
        }
        
        // 已过期
        if ("expired".equals(contractStatus)) {
            return "expired";
        }
        
        // 审核被拒
        if ("rejected".equals(auditStatus)) {
            return "rejected";
        }
        
        // 默认返回合同状态
        return contractStatus;
    }
}
