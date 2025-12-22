package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.ReportRequest;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.entity.Report;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.HouseMapper;
import org.example.rentingmanagement.mapper.ReportMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 举报管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final HouseMapper houseMapper;
    private final UserMapper userMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report submitReport(ReportRequest request, Long reporterId) {
        try {
            Report report = new Report();
            report.setReporterId(reporterId);
            report.setReportType(request.getReportType());
            report.setTargetId(request.getTargetId());
            report.setReasonType(request.getReasonType());
            report.setReasonDetail(request.getReasonDetail());
            report.setEvidenceImages(request.getEvidenceImages());
            report.setStatus("pending");
            report.setCreatedAt(LocalDateTime.now());

            this.save(report);
            log.info("举报提交成功: reportId={}, reporterId={}, targetId={}", 
                    report.getReportId(), reporterId, request.getTargetId());

            return report;

        } catch (Exception e) {
            log.error("提交举报失败: {}", e.getMessage(), e);
            throw new BusinessException("提交举报失败");
        }
    }

    @Override
    public List<Report> getReporterReports(Long reporterId) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId)
               .orderByDesc(Report::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawReport(Long reportId, Long reporterId) {
        Report report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }

        if (!report.getReporterId().equals(reporterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此举报");
        }

        if (!"pending".equals(report.getStatus())) {
            throw new BusinessException("该举报已处理，无法撤回");
        }

        report.setStatus("withdrawn");
        this.updateById(report);

        log.info("举报已撤回: reportId={}", reportId);
    }
    
    @Override
    public IPage<Map<String, Object>> getCommunityReports(Long communityId, String status, Integer pageNum, Integer pageSize) {
        // 查询该小区房源相关的举报
        Page<Report> page = new Page<>(pageNum, pageSize);
        
        // 先获取该小区的所有房源ID
        LambdaQueryWrapper<House> houseWrapper = new LambdaQueryWrapper<>();
        houseWrapper.eq(House::getCommunityId, communityId);
        List<House> communityHouses = houseMapper.selectList(houseWrapper);
        List<Long> houseIds = communityHouses.stream().map(House::getHouseId).toList();
        
        if (houseIds.isEmpty()) {
            Page<Map<String, Object>> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            return emptyPage;
        }
        
        // 查询这些房源的举报
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReportType, "house")
               .in(Report::getTargetId, houseIds);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        
        IPage<Report> reportPage = this.page(page, wrapper);
        
        // 转换为前端需要的格式
        return convertToDetailedPage(reportPage, communityHouses);
    }
    
    @Override
    public IPage<Map<String, Object>> getAllReports(String status, String reportType, Integer pageNum, Integer pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Report::getStatus, status);
        }
        if (reportType != null && !reportType.isEmpty()) {
            wrapper.eq(Report::getReportType, reportType);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        
        IPage<Report> reportPage = this.page(page, wrapper);
        
        // 转换为前端需要的格式
        return convertToDetailedPage(reportPage, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, Long handlerId, String action, String handleResult) {
        Report report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        
        // 根据action确定状态
        String newStatus;
        switch (action) {
            case "valid":
                newStatus = "handled";
                // 如果是房源举报且属实，下架房源
                if ("house".equals(report.getReportType())) {
                    House house = houseMapper.selectById(report.getTargetId());
                    if (house != null) {
                        house.setPublishStatus("offline");
                        house.setAuditStatus("rejected");
                        houseMapper.updateById(house);
                        log.info("举报属实，房源{}已下架", report.getTargetId());
                    }
                }
                break;
            case "warning":
                newStatus = "handled";
                // 警告房东，不下架房源
                break;
            case "invalid":
                newStatus = "rejected";
                break;
            default:
                newStatus = "handled";
        }
        
        report.setStatus(newStatus);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());
        this.updateById(report);
        
        log.info("举报处理完成: reportId={}, action={}, handlerId={}", reportId, action, handlerId);
    }
    
    /**
     * 将Report分页转换为包含详细信息的Map分页
     */
    private IPage<Map<String, Object>> convertToDetailedPage(IPage<Report> reportPage, List<House> communityHouses) {
        List<Map<String, Object>> detailedList = new ArrayList<>();
        
        for (Report report : reportPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("reportId", report.getReportId());
            item.put("reportType", report.getReasonType());
            item.put("status", report.getStatus());
            item.put("reportTime", report.getCreatedAt() != null ? report.getCreatedAt().format(DATE_FORMATTER) : "");
            item.put("description", report.getReasonDetail());
            item.put("handleResult", report.getHandleResult());
            
            // 解析证据图片
            if (report.getEvidenceImages() != null && !report.getEvidenceImages().isEmpty()) {
                try {
                    item.put("evidence", com.alibaba.fastjson2.JSON.parseArray(report.getEvidenceImages(), String.class));
                } catch (Exception e) {
                    item.put("evidence", new ArrayList<>());
                }
            } else {
                item.put("evidence", new ArrayList<>());
            }
            
            // 获取举报人信息
            User reporter = userMapper.selectById(report.getReporterId());
            if (reporter != null) {
                item.put("reporter", reporter.getNickname());
                item.put("reporterPhone", reporter.getPhone());
            } else {
                item.put("reporter", "未知用户");
                item.put("reporterPhone", "");
            }
            
            // 如果是房源举报，获取房源和房东信息
            if ("house".equals(report.getReportType())) {
                House house = null;
                if (communityHouses != null) {
                    house = communityHouses.stream()
                            .filter(h -> h.getHouseId().equals(report.getTargetId()))
                            .findFirst()
                            .orElse(null);
                }
                if (house == null) {
                    house = houseMapper.selectById(report.getTargetId());
                }
                
                if (house != null) {
                    item.put("houseTitle", house.getTitle());
                    item.put("houseId", house.getHouseId());
                    
                    // 获取房东信息
                    User landlord = userMapper.selectById(house.getLandlordId());
                    if (landlord != null) {
                        item.put("landlord", landlord.getNickname());
                        item.put("landlordPhone", landlord.getPhone());
                    } else {
                        item.put("landlord", "未知房东");
                        item.put("landlordPhone", "");
                    }
                } else {
                    item.put("houseTitle", "房源已删除");
                    item.put("houseId", report.getTargetId());
                    item.put("landlord", "未知");
                    item.put("landlordPhone", "");
                }
            }
            
            detailedList.add(item);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(reportPage.getCurrent(), reportPage.getSize());
        resultPage.setRecords(detailedList);
        resultPage.setTotal(reportPage.getTotal());
        
        return resultPage;
    }
}
