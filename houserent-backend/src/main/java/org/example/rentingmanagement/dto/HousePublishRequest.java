package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 房源发布请求DTO
 */
@Data
public class HousePublishRequest {

    /**
     * 小区ID
     */
    @NotNull(message = "小区ID不能为空")
    private Long communityId;

    /**
     * 房源标题
     */
    @NotBlank(message = "房源标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    /**
     * 户型
     */
    @NotBlank(message = "户型不能为空")
    private String houseType;

    /**
     * 面积
     */
    @NotNull(message = "面积不能为空")
    @DecimalMin(value = "0.1", message = "面积必须大于0")
    private BigDecimal area;

    /**
     * 楼层
     */
    private String floor;

    /**
     * 总楼层
     */
    private String totalFloor;

    /**
     * 朝向
     */
    private String orientation;

    /**
     * 装修情况
     */
    private String decoration;

    /**
     * 租金
     */
    @NotNull(message = "租金不能为空")
    @DecimalMin(value = "0.01", message = "租金必须大于0")
    private BigDecimal rentPrice;

    /**
     * 支付方式
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 租赁期限：短期, 长期
     */
    private String rentPeriod;

    /**
     * 房屋描述
     */
    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    /**
     * 配套设施
     */
    private List<String> facilities;

    /**
     * 房屋图片URL列表 - 图片通过单独接口上传，此字段可为空
     */
    @Size(max = 6, message = "最多上传6张图片")
    private List<String> images;
}
