package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.HouseFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房源收藏数据访问层
 */
@Mapper
public interface HouseFavoriteMapper extends BaseMapper<HouseFavorite> {

    /**
     * 查询用户收藏的房源列表
     */
    @Select("SELECT hf.*, h.* FROM house_favorites hf " +
            "LEFT JOIN houses h ON hf.house_id = h.house_id " +
            "WHERE hf.user_id = #{userId} AND h.audit_status = 'approved' " +
            "ORDER BY hf.created_at DESC")
    List<HouseFavorite> findUserFavorites(Long userId);

    /**
     * 检查用户是否已收藏某房源
     */
    @Select("SELECT COUNT(*) > 0 FROM house_favorites WHERE user_id = #{userId} AND house_id = #{houseId}")
    boolean isFavorited(Long userId, Long houseId);

    /**
     * 统计房源收藏数
     */
    @Select("SELECT COUNT(*) FROM house_favorites WHERE house_id = #{houseId}")
    int countByHouseId(Long houseId);
}
