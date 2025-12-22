package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.CommunityFacility;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小区配套设施数据访问层
 */
@Mapper
public interface CommunityFacilityMapper extends BaseMapper<CommunityFacility> {

    /**
     * 根据小区ID查询配套设施
     */
    @Select("SELECT * FROM community_facilities WHERE community_id = #{communityId} AND status = 'active' ORDER BY facility_type, category")
    List<CommunityFacility> findByCommunityId(Long communityId);

    /**
     * 根据小区ID和配套类型查询
     */
    @Select("SELECT * FROM community_facilities WHERE community_id = #{communityId} AND facility_type = #{facilityType} AND status = 'active' ORDER BY category")
    List<CommunityFacility> findByCommunityIdAndType(Long communityId, String facilityType);

    /**
     * 根据分类查询配套设施
     */
    @Select("SELECT * FROM community_facilities WHERE community_id = #{communityId} AND category = #{category} AND status = 'active'")
    List<CommunityFacility> findByCommunityIdAndCategory(Long communityId, String category);
}
