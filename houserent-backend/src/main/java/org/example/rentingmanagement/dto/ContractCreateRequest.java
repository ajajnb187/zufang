package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合同创建请求DTO
 */
@Data
public class ContractCreateRequest {

    /**
     * 房源ID
     */
    @NotNull(message = "房源ID不能为空")
    private Long houseId;

    /**
     * 租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 租赁开始日期
     */
    @NotNull(message = "租赁开始日期不能为空")
    private LocalDate startDate;

    /**
     * 租赁结束日期
     */
    @NotNull(message = "租赁结束日期不能为空")
    private LocalDate endDate;

    /**
     * 租期月数
     */
    private Integer months;

    /**
     * 月租金
     */
    private BigDecimal monthlyRent;

    /**
     * 押金
     */
    private BigDecimal deposit;

    /**
     * 付款方式（押一付一、押一付三等）
     */
    private String paymentMethod;

    /**
     * 补充条款
     */
    private String customContent;
}
