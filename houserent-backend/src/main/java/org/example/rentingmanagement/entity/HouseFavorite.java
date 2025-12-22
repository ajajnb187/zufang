package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 房源收藏实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("house_favorites")
public class HouseFavorite {

    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Long favoriteId;

    @TableField("user_id")
    private Long userId;

    @TableField("house_id")
    private Long houseId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
