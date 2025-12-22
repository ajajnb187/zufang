package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小区配套设施实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_facilities")
public class CommunityFacility {

    @TableId(value = "facility_id", type = IdType.AUTO)
    private Long facilityId;

    @TableField("community_id")
    private Long communityId;

    @TableField("facility_type")
    private String facilityType; // internal, surrounding

    @TableField("category")
    private String category;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("images")
    private String images; // JSON数组

    @TableField("contact_info")
    private String contactInfo;

    @TableField("distance")
    private String distance;

    @TableField("location")
    private String location;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("status")
    private String status; // active, inactive

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
