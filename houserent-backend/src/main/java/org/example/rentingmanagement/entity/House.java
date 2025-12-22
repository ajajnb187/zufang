package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("houses")
public class House {

    @TableId(value = "house_id", type = IdType.AUTO)
    private Long houseId;

    @TableField("landlord_id")
    private Long landlordId;

    @TableField("community_id")
    private Long communityId;

    @TableField("title")
    private String title;

    @TableField("house_type")
    private String houseType;

    @TableField("area")
    private BigDecimal area;

    @TableField("floor")
    private String floor;

    @TableField("total_floor")
    private String totalFloor;

    @TableField("orientation")
    private String orientation;

    @TableField("decoration")
    private String decoration; // rough, simple, fine, luxury

    @TableField("rent_price")
    private BigDecimal rentPrice;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("rent_period")
    private String rentPeriod;

    @TableField("description")
    private String description;

    @TableField("images")
    private String images; // JSON数组

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("facilities")
    private String facilities; // JSON数组

    @TableField("audit_status")
    private String auditStatus; // draft, pending, approved, rejected

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("audit_opinion")
    private String auditOpinion;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("publish_status")
    private String publishStatus; // online, offline, rented

    @TableField("view_count")
    private Integer viewCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
