package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.rentingmanagement.dto.ReportRequest;
import org.example.rentingmanagement.entity.Report;

import java.util.List;
import java.util.Map;

/**
 * 举报管理服务接口
 */
public interface ReportService extends IService<Report> {

    /**
     * 提交举报
     */
    Report submitReport(ReportRequest request, Long reporterId);

    /**
     * 获取举报人的举报列表
     */
    List<Report> getReporterReports(Long reporterId);

    /**
     * 撤回举报
     */
    void withdrawReport(Long reportId, Long reporterId);
    
    /**
     * 获取小区相关的举报列表（小区管理员）
     */
    IPage<Map<String, Object>> getCommunityReports(Long communityId, String status, Integer pageNum, Integer pageSize);
    
    /**
     * 获取所有举报列表（平台管理员）
     */
    IPage<Map<String, Object>> getAllReports(String status, String reportType, Integer pageNum, Integer pageSize);
    
    /**
     * 处理举报
     */
    void handleReport(Long reportId, Long handlerId, String action, String handleResult);
}
