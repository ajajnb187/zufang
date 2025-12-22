package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.HouseMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.RevenueService;
import org.example.rentingmanagement.vo.RevenueDetailVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收益管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final RentalContractMapper contractMapper;
    private final HouseMapper houseMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> getLandlordRevenueStatistics(Long landlordId, String period) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取所有有效合同
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RentalContract::getLandlordId, landlordId)
                   .eq(RentalContract::getContractStatus, "effective");
            
            List<RentalContract> contracts = contractMapper.selectList(wrapper);
            
            // 计算总收益（所有有效合同的租金总和）
            BigDecimal totalRevenue = contracts.stream()
                    .map(RentalContract::getRentPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("totalRevenue", totalRevenue);
            
            // 计算本月收益（当前有效合同的租金）
            LocalDate now = LocalDate.now();
            BigDecimal monthRevenue = contracts.stream()
                    .filter(contract -> contract.getStartDate() != null && 
                           contract.getEndDate() != null &&
                           !contract.getStartDate().isAfter(now) &&
                           !contract.getEndDate().isBefore(now))
                    .map(RentalContract::getRentPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("monthRevenue", monthRevenue);
            
            // 今日收益（简化为0，实际需要支付记录表）
            statistics.put("todayRevenue", BigDecimal.ZERO);
            
            // 待收收益（即将到期需要续费的合同）
            LocalDate nextMonth = now.plusMonths(1);
            BigDecimal pendingRevenue = contracts.stream()
                    .filter(contract -> contract.getEndDate() != null &&
                           contract.getEndDate().isBefore(nextMonth) &&
                           contract.getEndDate().isAfter(now))
                    .map(RentalContract::getRentPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("pendingRevenue", pendingRevenue);
            
            // 已收收益（当前月收益）
            statistics.put("receivedRevenue", monthRevenue);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取房东收益统计失败: landlordId={}", landlordId, e);
            // 返回默认值
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalRevenue", BigDecimal.ZERO);
            defaultStats.put("monthRevenue", BigDecimal.ZERO);
            defaultStats.put("todayRevenue", BigDecimal.ZERO);
            defaultStats.put("pendingRevenue", BigDecimal.ZERO);
            defaultStats.put("receivedRevenue", BigDecimal.ZERO);
            return defaultStats;
        }
    }

    @Override
    public IPage<RevenueDetailVO> getLandlordRevenueDetails(Long landlordId, String startDate, String endDate, Integer pageNum, Integer pageSize) {
        try {
            Page<RentalContract> page = new Page<>(pageNum, pageSize);
            
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RentalContract::getLandlordId, landlordId)
                   .eq(RentalContract::getContractStatus, "effective");
            
            // 日期范围过滤
            if (startDate != null && !startDate.trim().isEmpty()) {
                LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                wrapper.ge(RentalContract::getStartDate, start);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                wrapper.le(RentalContract::getEndDate, end);
            }
            
            wrapper.orderByDesc(RentalContract::getCreatedAt);
            
            IPage<RentalContract> contractPage = contractMapper.selectPage(page, wrapper);
            
            // 转换为收益明细VO
            List<RevenueDetailVO> revenueList = contractPage.getRecords().stream()
                    .map(this::convertToRevenueDetailVO)
                    .collect(Collectors.toList());
            
            Page<RevenueDetailVO> resultPage = new Page<>(pageNum, pageSize);
            resultPage.setRecords(revenueList);
            resultPage.setTotal(contractPage.getTotal());
            
            return resultPage;
            
        } catch (Exception e) {
            log.error("获取房东收益明细失败: landlordId={}", landlordId, e);
            return new Page<>(pageNum, pageSize);
        }
    }

    @Override
    public List<Map<String, Object>> getHouseRevenueRanking(Long landlordId) {
        try {
            // 获取房东所有有效合同
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RentalContract::getLandlordId, landlordId)
                   .eq(RentalContract::getContractStatus, "effective");
            
            List<RentalContract> contracts = contractMapper.selectList(wrapper);
            
            // 按房源分组统计收益
            Map<Long, List<RentalContract>> houseContractsMap = contracts.stream()
                    .collect(Collectors.groupingBy(RentalContract::getHouseId));
            
            List<Map<String, Object>> ranking = new ArrayList<>();
            
            for (Map.Entry<Long, List<RentalContract>> entry : houseContractsMap.entrySet()) {
                Long houseId = entry.getKey();
                List<RentalContract> houseContracts = entry.getValue();
                
                // 获取房源信息
                House house = houseMapper.selectById(houseId);
                if (house == null) continue;
                
                // 计算该房源总收益
                BigDecimal totalRevenue = houseContracts.stream()
                        .map(RentalContract::getRentPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> houseRevenue = new HashMap<>();
                houseRevenue.put("houseId", houseId);
                houseRevenue.put("houseTitle", house.getTitle());
                houseRevenue.put("totalRevenue", totalRevenue);
                houseRevenue.put("contractCount", houseContracts.size());
                
                ranking.add(houseRevenue);
            }
            
            // 按收益降序排序
            ranking.sort((a, b) -> {
                BigDecimal revenueA = (BigDecimal) a.get("totalRevenue");
                BigDecimal revenueB = (BigDecimal) b.get("totalRevenue");
                return revenueB.compareTo(revenueA);
            });
            
            return ranking;
            
        } catch (Exception e) {
            log.error("获取房源收益排行失败: landlordId={}", landlordId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换合同为收益明细VO
     */
    private RevenueDetailVO convertToRevenueDetailVO(RentalContract contract) {
        RevenueDetailVO revenueVO = new RevenueDetailVO();
        
        try {
            revenueVO.setRevenueId(contract.getContractId()); // 使用合同ID作为收益ID
            revenueVO.setHouseId(contract.getHouseId());
            revenueVO.setTenantId(contract.getTenantId());
            revenueVO.setAmount(contract.getRentPrice());
            revenueVO.setRevenueType("租金");
            revenueVO.setRevenueDate(contract.getStartDate());
            revenueVO.setPaymentStatus("已支付"); // 简化处理
            revenueVO.setCreatedAt(contract.getCreatedAt());
            
            // 获取房源信息
            House house = houseMapper.selectById(contract.getHouseId());
            if (house != null) {
                revenueVO.setHouseTitle(house.getTitle());
            }
            
            // 获取租客信息
            User tenant = userMapper.selectById(contract.getTenantId());
            if (tenant != null) {
                revenueVO.setTenantName(tenant.getNickname());
            }
            
        } catch (Exception e) {
            log.error("转换收益明细VO失败: contractId={}", contract.getContractId(), e);
        }
        
        return revenueVO;
    }
}
