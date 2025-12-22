package org.example.rentingmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约看房请求DTO
 */
@Data
public class AppointmentRequest {

    /**
     * 房源ID
     */
    @NotNull(message = "房源ID不能为空")
    private Long houseId;

    /**
     * 预约时间
     */
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;

    /**
     * 联系电话
     */
    @NotNull(message = "联系电话不能为空")
    private String contactPhone;

    /**
     * 留言备注
     */
    private String message;
}
