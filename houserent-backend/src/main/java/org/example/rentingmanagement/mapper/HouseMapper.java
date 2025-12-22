package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.rentingmanagement.entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 房源数据访问层
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

    /**
     * 分页查询房源列表（带搜索条件，支持地区筛选）
     */
    @Select("<script>" +
            "SELECT h.* FROM houses h " +
            "LEFT JOIN communities c ON h.community_id = c.community_id " +
            "WHERE h.audit_status = 'approved' AND h.publish_status = 'online' " +
            "<if test='communityId != null'>AND h.community_id = #{communityId} </if>" +
            "<if test='district != null and district != \"\"'>AND c.district = #{district} </if>" +
            "<if test='city != null and city != \"\"'>AND c.city = #{city} </if>" +
            "<if test='province != null and province != \"\"'>AND c.province = #{province} </if>" +
            "<if test='rentPeriod != null and rentPeriod != \"\"'>AND h.rent_period = #{rentPeriod} </if>" +
            "<if test='houseType != null and houseType != \"\"'>AND h.house_type LIKE CONCAT('%', #{houseType}, '%') </if>" +
            "<if test='minPrice != null'>AND h.rent_price &gt;= #{minPrice} </if>" +
            "<if test='maxPrice != null'>AND h.rent_price &lt;= #{maxPrice} </if>" +
            "<if test='minArea != null'>AND h.area &gt;= #{minArea} </if>" +
            "<if test='maxArea != null'>AND h.area &lt;= #{maxArea} </if>" +
            "ORDER BY h.created_at DESC" +
            "</script>")
    IPage<House> findHousesWithConditions(Page<House> page,
                                          @Param("communityId") Long communityId,
                                          @Param("province") String province,
                                          @Param("city") String city,
                                          @Param("district") String district,
                                          @Param("rentPeriod") String rentPeriod,
                                          @Param("houseType") String houseType,
                                          @Param("minPrice") Double minPrice,
                                          @Param("maxPrice") Double maxPrice,
                                          @Param("minArea") Double minArea,
                                          @Param("maxArea") Double maxArea);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE houses SET view_count = view_count + 1 WHERE house_id = #{houseId}")
    void incrementViewCount(Long houseId);

    /**
     * 更新收藏次数
     */
    @Update("UPDATE houses SET favorite_count = #{count} WHERE house_id = #{houseId}")
    void updateFavoriteCount(@Param("houseId") Long houseId, @Param("count") Integer count);
}
