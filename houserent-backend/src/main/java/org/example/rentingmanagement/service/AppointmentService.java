package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.rentingmanagement.dto.AppointmentRequest;
import org.example.rentingmanagement.entity.HouseAppointment;

import java.util.List;

/**
 * 预约看房服务接口
 */
public interface AppointmentService extends IService<HouseAppointment> {

    /**
     * 创建预约
     */
    HouseAppointment createAppointment(AppointmentRequest request, Long tenantId);

    /**
     * 获取租客的预约列表
     */
    List<HouseAppointment> getTenantAppointments(Long tenantId);

    /**
     * 获取房东的预约列表
     */
    List<HouseAppointment> getLandlordAppointments(Long landlordId);

    /**
     * 确认预约
     */
    void confirmAppointment(Long appointmentId, Long landlordId);

    /**
     * 取消预约
     */
    void cancelAppointment(Long appointmentId, Long userId);

    /**
     * 标记预约完成（房东标记看房完成）
     */
    void completeAppointment(Long appointmentId, Long landlordId);

    /**
     * 拒绝预约
     */
    void rejectAppointment(Long appointmentId, Long landlordId, String reason);
}
