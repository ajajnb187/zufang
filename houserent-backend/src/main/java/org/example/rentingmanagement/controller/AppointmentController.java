package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.AppointmentRequest;
import org.example.rentingmanagement.entity.HouseAppointment;
import org.example.rentingmanagement.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约看房控制器
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 创建预约
     */
    @PostMapping("/create")
    public Result<HouseAppointment> createAppointment(@RequestBody @Valid AppointmentRequest request) {
        Long tenantId = StpUtil.getLoginIdAsLong();
        HouseAppointment appointment = appointmentService.createAppointment(request, tenantId);
        return Result.success("预约成功", appointment);
    }

    /**
     * 获取我的预约列表（租客）
     */
    @GetMapping("/my-appointments")
    public Result<List<HouseAppointment>> getMyAppointments() {
        Long tenantId = StpUtil.getLoginIdAsLong();
        List<HouseAppointment> appointments = appointmentService.getTenantAppointments(tenantId);
        return Result.success(appointments);
    }

    /**
     * 获取房东的预约列表
     */
    @GetMapping("/landlord-appointments")
    public Result<List<HouseAppointment>> getLandlordAppointments() {
        Long landlordId = StpUtil.getLoginIdAsLong();
        List<HouseAppointment> appointments = appointmentService.getLandlordAppointments(landlordId);
        return Result.success(appointments);
    }

    /**
     * 确认预约（房东）
     */
    @PostMapping("/{appointmentId}/confirm")
    public Result<Void> confirmAppointment(@PathVariable Long appointmentId) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        appointmentService.confirmAppointment(appointmentId, landlordId);
        return Result.success("预约已确认");
    }

    /**
     * 取消预约
     */
    @PostMapping("/{appointmentId}/cancel")
    public Result<Void> cancelAppointment(@PathVariable Long appointmentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        appointmentService.cancelAppointment(appointmentId, userId);
        return Result.success("预约已取消");
    }

    /**
     * 标记预约完成（房东标记看房完成）
     */
    @PostMapping("/{appointmentId}/complete")
    public Result<Void> completeAppointment(@PathVariable Long appointmentId) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        appointmentService.completeAppointment(appointmentId, landlordId);
        return Result.success("已标记看房完成");
    }

    /**
     * 拒绝预约（房东）
     */
    @PostMapping("/{appointmentId}/reject")
    public Result<Void> rejectAppointment(@PathVariable Long appointmentId,
                                          @RequestParam(required = false) String reason) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        appointmentService.rejectAppointment(appointmentId, landlordId, reason);
        return Result.success("已拒绝预约");
    }
}
