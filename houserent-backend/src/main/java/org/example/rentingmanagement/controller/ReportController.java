package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.ReportRequest;
import org.example.rentingmanagement.entity.Report;
import org.example.rentingmanagement.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 举报管理控制器
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public Result<Report> submitReport(@RequestBody @Valid ReportRequest request) {
        Long reporterId = StpUtil.getLoginIdAsLong();
        Report report = reportService.submitReport(request, reporterId);
        return Result.success("举报提交成功，我们会尽快处理", report);
    }

    /**
     * 获取我的举报列表
     */
    @GetMapping("/my-reports")
    public Result<List<Report>> getMyReports() {
        Long reporterId = StpUtil.getLoginIdAsLong();
        List<Report> reports = reportService.getReporterReports(reporterId);
        return Result.success(reports);
    }

    /**
     * 撤回举报
     */
    @PostMapping("/{reportId}/withdraw")
    public Result<Void> withdrawReport(@PathVariable Long reportId) {
        Long reporterId = StpUtil.getLoginIdAsLong();
        reportService.withdrawReport(reportId, reporterId);
        return Result.success("举报已撤回");
    }
}
