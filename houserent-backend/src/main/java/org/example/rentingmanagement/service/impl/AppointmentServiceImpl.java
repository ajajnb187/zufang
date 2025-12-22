package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.AppointmentRequest;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.entity.HouseAppointment;
import org.example.rentingmanagement.mapper.HouseAppointmentMapper;
import org.example.rentingmanagement.mapper.HouseMapper;
import org.example.rentingmanagement.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约看房服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<HouseAppointmentMapper, HouseAppointment> 
        implements AppointmentService {

    private final HouseMapper houseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HouseAppointment createAppointment(AppointmentRequest request, Long tenantId) {
        try {
            // 检查房源是否存在
            House house = houseMapper.selectById(request.getHouseId());
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 检查房源状态
            if (!"approved".equals(house.getAuditStatus()) || !"online".equals(house.getPublishStatus())) {
                throw new BusinessException("该房源已下架或未审核通过");
            }

            // 创建预约记录
            HouseAppointment appointment = new HouseAppointment();
            appointment.setHouseId(request.getHouseId());
            appointment.setTenantId(tenantId);
            appointment.setLandlordId(house.getLandlordId());
            appointment.setAppointmentTime(request.getAppointmentTime());
            appointment.setContactPhone(request.getContactPhone());
            appointment.setMessage(request.getMessage());
            appointment.setStatus("pending");
            appointment.setCreatedAt(LocalDateTime.now());

            this.save(appointment);
            log.info("创建预约成功: appointmentId={}, tenantId={}, houseId={}", 
                    appointment.getAppointmentId(), tenantId, request.getHouseId());

            return appointment;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建预约失败: {}", e.getMessage(), e);
            throw new BusinessException("创建预约失败");
        }
    }

    @Override
    public List<HouseAppointment> getTenantAppointments(Long tenantId) {
        return this.baseMapper.findByTenantIdWithDetails(tenantId);
    }

    @Override
    public List<HouseAppointment> getLandlordAppointments(Long landlordId) {
        return this.baseMapper.findByLandlordIdWithDetails(landlordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(Long appointmentId, Long landlordId) {
        HouseAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!appointment.getLandlordId().equals(landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约");
        }

        if (!"pending".equals(appointment.getStatus())) {
            throw new BusinessException("该预约已处理");
        }

        appointment.setStatus("confirmed");
        appointment.setUpdatedAt(LocalDateTime.now());
        this.updateById(appointment);

        log.info("确认预约成功: appointmentId={}", appointmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long appointmentId, Long userId) {
        HouseAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        // 只有租客或房东可以取消
        if (!appointment.getTenantId().equals(userId) && !appointment.getLandlordId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约");
        }

        appointment.setStatus("cancelled");
        appointment.setUpdatedAt(LocalDateTime.now());
        this.updateById(appointment);

        log.info("取消预约成功: appointmentId={}", appointmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAppointment(Long appointmentId, Long landlordId) {
        HouseAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!appointment.getLandlordId().equals(landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约");
        }

        if (!"confirmed".equals(appointment.getStatus())) {
            throw new BusinessException("只有已确认的预约才能标记完成");
        }

        appointment.setStatus("completed");
        appointment.setUpdatedAt(LocalDateTime.now());
        this.updateById(appointment);

        log.info("标记预约完成: appointmentId={}", appointmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAppointment(Long appointmentId, Long landlordId, String reason) {
        HouseAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!appointment.getLandlordId().equals(landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约");
        }

        if (!"pending".equals(appointment.getStatus())) {
            throw new BusinessException("该预约已处理");
        }

        appointment.setStatus("rejected");
        appointment.setUpdatedAt(LocalDateTime.now());
        this.updateById(appointment);

        log.info("拒绝预约: appointmentId={}, reason={}", appointmentId, reason);
    }
}
