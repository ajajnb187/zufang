package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.Community;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小区数据访问层
 */
@Mapper
public interface CommunityMapper extends BaseMapper<Community> {

    /**
     * 根据城市查询小区列表
     */
    @Select("SELECT * FROM communities WHERE city = #{city} AND status = 'active' ORDER BY community_name")
    List<Community> findByCity(String city);

    /**
     * 搜索小区
     */
    @Select("SELECT * FROM communities WHERE community_name LIKE CONCAT('%', #{keyword}, '%') AND status = 'active' LIMIT 20")
    List<Community> searchCommunities(String keyword);
}
