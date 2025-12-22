package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.CommunityFacility;

import java.util.List;

/**
 * 小区信息管理服务接口
 */
public interface CommunityService {

    /**
     * 获取小区列表（分页）
     */
    IPage<Community> getCommunityList(String name, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取小区详情
     */
    Community getCommunityDetail(Long communityId);

    /**
     * 搜索小区
     */
    List<Community> searchCommunities(String keyword);

    /**
     * 根据城市获取小区列表
     */
    List<Community> getCommunitiesByCity(String city);

    /**
     * 获取小区配套设施
     */
    List<CommunityFacility> getCommunityFacilities(Long communityId, String facilityType);

    /**
     * 添加小区配套设施
     */
    CommunityFacility addCommunityFacility(Long communityId, CommunityFacility facility, Long creatorId);

    /**
     * 更新小区配套设施
     */
    CommunityFacility updateCommunityFacility(Long facilityId, CommunityFacility facility, Long userId);

    /**
     * 删除小区配套设施
     */
    boolean deleteCommunityFacility(Long facilityId, Long userId);

    /**
     * 获取热门小区列表
     */
    List<Community> getPopularCommunities(int limit);

    /**
     * 检查用户是否可以管理小区配套设施
     */
    boolean canManageFacilities(Long userId, Long communityId);

    /**
     * 保存小区
     */
    boolean save(Community community);

    /**
     * 根据ID获取小区
     */
    Community getById(Long communityId);

    /**
     * 根据ID更新小区
     */
    boolean updateById(Community community);
}
