package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小区实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("communities")
public class Community {

    @TableId(value = "community_id", type = IdType.AUTO)
    private Long communityId;

    @TableField("community_name")
    private String communityName;

    @TableField("address")
    private String address;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("property_company")
    private String propertyCompany;

    @TableField("property_phone")
    private String propertyPhone;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status; // active, inactive

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
