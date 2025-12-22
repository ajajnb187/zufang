package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 房源图片实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("house_images")
public class HouseImage {

    /**
     * 图片ID
     */
    @TableId(value = "image_id", type = IdType.AUTO)
    private Long imageId;

    /**
     * 房源ID
     */
    @TableField("house_id")
    private Long houseId;

    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 图片原始名称
     */
    @TableField("image_name")
    private String imageName;

    /**
     * 图片大小(字节)
     */
    @TableField("image_size")
    private Long imageSize;

    /**
     * 图片类型(MIME)
     */
    @TableField("image_type")
    private String imageType;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否为封面图
     */
    @TableField("is_cover")
    private Boolean isCover;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
