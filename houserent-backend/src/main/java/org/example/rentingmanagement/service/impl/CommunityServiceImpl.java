package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.CommunityFacility;
import org.example.rentingmanagement.mapper.AdminMapper;
import org.example.rentingmanagement.mapper.CommunityFacilityMapper;
import org.example.rentingmanagement.mapper.CommunityMapper;
import org.example.rentingmanagement.mapper.CommunityVerificationMapper;
import org.example.rentingmanagement.service.CommunityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小区信息管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl extends ServiceImpl<CommunityMapper, Community> implements CommunityService {

    private final CommunityFacilityMapper communityFacilityMapper;
    private final CommunityVerificationMapper communityVerificationMapper;
    private final AdminMapper adminMapper;

    @Override
    public IPage<Community> getCommunityList(String name, String status, Integer pageNum, Integer pageSize) {
        try {
            Page<Community> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
            
            // 按状态筛选
            if (status != null && !status.isEmpty()) {
                wrapper.eq(Community::getStatus, status);
            } else {
                // 默认只查询活跃状态的小区
                wrapper.eq(Community::getStatus, "active");
            }
            
            // 按小区名称搜索
            if (name != null && !name.isEmpty()) {
                wrapper.like(Community::getCommunityName, name);
            }
            
            wrapper.orderByDesc(Community::getCreatedAt);
            
            return this.page(page, wrapper);
            
        } catch (Exception e) {
            log.error("获取小区列表失败: name={}, status={}", name, status, e);
            throw new BusinessException("获取小区列表失败");
        }
    }

    @Override
    public Community getCommunityDetail(Long communityId) {
        try {
            Community community = this.getById(communityId);
            if (community == null) {
                throw new BusinessException("小区不存在");
            }
            
            if (!"active".equals(community.getStatus())) {
                throw new BusinessException("小区信息不可用");
            }
            
            return community;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取小区详情失败: communityId={}", communityId, e);
            throw new BusinessException("获取小区详情失败");
        }
    }

    @Override
    public List<Community> searchCommunities(String keyword) {
        try {
            return baseMapper.searchCommunities(keyword);
        } catch (Exception e) {
            log.error("搜索小区失败: keyword={}", keyword, e);
            throw new BusinessException("搜索小区失败");
        }
    }

    @Override
    public List<Community> getCommunitiesByCity(String city) {
        try {
            return baseMapper.findByCity(city);
        } catch (Exception e) {
            log.error("获取城市小区列表失败: city={}", city, e);
            throw new BusinessException("获取小区列表失败");
        }
    }

    @Override
    public List<CommunityFacility> getCommunityFacilities(Long communityId, String facilityType) {
        try {
            if (facilityType != null && !facilityType.isEmpty()) {
                return communityFacilityMapper.findByCommunityIdAndType(communityId, facilityType);
            } else {
                return communityFacilityMapper.findByCommunityId(communityId);
            }
        } catch (Exception e) {
            log.error("获取小区配套设施失败: communityId={}, facilityType={}", communityId, facilityType, e);
            throw new BusinessException("获取配套设施失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityFacility addCommunityFacility(Long communityId, CommunityFacility facility, Long creatorId) {
        try {
            // 检查小区是否存在
            Community community = this.getById(communityId);
            if (community == null) {
                throw new BusinessException("小区不存在");
            }

            // 检查用户权限
            if (!canManageFacilities(creatorId, communityId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权添加小区配套设施");
            }

            // 设置基本信息
            facility.setCommunityId(communityId);
            facility.setCreatorId(creatorId);
            facility.setStatus("active");

            communityFacilityMapper.insert(facility);

            log.info("小区配套设施添加成功: facilityId={}, communityId={}, creatorId={}", 
                    facility.getFacilityId(), communityId, creatorId);
            return facility;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加小区配套设施失败: communityId={}, creatorId={}", communityId, creatorId, e);
            throw new BusinessException("添加配套设施失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityFacility updateCommunityFacility(Long facilityId, CommunityFacility facility, Long userId) {
        try {
            CommunityFacility existingFacility = communityFacilityMapper.selectById(facilityId);
            if (existingFacility == null) {
                throw new BusinessException("配套设施不存在");
            }

            // 检查用户权限（创建者或小区管理员）
            if (!existingFacility.getCreatorId().equals(userId) && 
                !canManageFacilities(userId, existingFacility.getCommunityId())) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此配套设施");
            }

            // 更新设施信息
            existingFacility.setName(facility.getName());
            existingFacility.setDescription(facility.getDescription());
            existingFacility.setCategory(facility.getCategory());
            existingFacility.setImages(facility.getImages());
            existingFacility.setContactInfo(facility.getContactInfo());
            existingFacility.setDistance(facility.getDistance());
            existingFacility.setLocation(facility.getLocation());
            existingFacility.setLongitude(facility.getLongitude());
            existingFacility.setLatitude(facility.getLatitude());

            communityFacilityMapper.updateById(existingFacility);

            log.info("小区配套设施更新成功: facilityId={}, userId={}", facilityId, userId);
            return existingFacility;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新小区配套设施失败: facilityId={}, userId={}", facilityId, userId, e);
            throw new BusinessException("更新配套设施失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCommunityFacility(Long facilityId, Long userId) {
        try {
            CommunityFacility facility = communityFacilityMapper.selectById(facilityId);
            if (facility == null) {
                throw new BusinessException("配套设施不存在");
            }

            // 检查用户权限（创建者或小区管理员）
            if (!facility.getCreatorId().equals(userId) && 
                !canManageFacilities(userId, facility.getCommunityId())) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此配套设施");
            }

            // 软删除
            facility.setStatus("inactive");
            communityFacilityMapper.updateById(facility);

            log.info("小区配套设施删除成功: facilityId={}, userId={}", facilityId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除小区配套设施失败: facilityId={}, userId={}", facilityId, userId, e);
            throw new BusinessException("删除配套设施失败");
        }
    }

    @Override
    public List<Community> getPopularCommunities(int limit) {
        try {
            // 这里可以根据房源数量、用户活跃度等指标来排序
            // 简化实现，按创建时间排序
            LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Community::getStatus, "active")
                   .orderByDesc(Community::getCreatedAt)
                   .last("LIMIT " + limit);
            
            return this.list(wrapper);
            
        } catch (Exception e) {
            log.error("获取热门小区列表失败: limit={}", limit, e);
            throw new BusinessException("获取热门小区失败");
        }
    }

    @Override
    public boolean canManageFacilities(Long userId, Long communityId) {
        try {
            // 检查是否是小区管理员
            List<org.example.rentingmanagement.entity.Admin> admins = adminMapper.findByCommunityId(communityId);
            for (org.example.rentingmanagement.entity.Admin admin : admins) {
                if (admin.getUserId().equals(userId)) {
                    return true;
                }
            }

            // 检查是否是平台管理员
            List<org.example.rentingmanagement.entity.Admin> platformAdmins = adminMapper.findPlatformAdmins();
            for (org.example.rentingmanagement.entity.Admin admin : platformAdmins) {
                if (admin.getUserId().equals(userId)) {
                    return true;
                }
            }

            // 检查是否是已认证的小区用户（可以添加设施信息）
            return communityVerificationMapper.isUserVerified(userId, communityId);

        } catch (Exception e) {
            log.error("检查配套设施管理权限失败: userId={}, communityId={}", userId, communityId, e);
            return false;
        }
    }
}
