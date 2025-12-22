package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.CommunityFacility;
import org.example.rentingmanagement.entity.FacilityFeedback;
import org.example.rentingmanagement.mapper.FacilityFeedbackMapper;
import org.example.rentingmanagement.service.CommunityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小区信息管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final FacilityFeedbackMapper facilityFeedbackMapper;

    /**
     * 获取小区列表（公开接口）
     */
    @GetMapping("/list")
    public Result<IPage<Community>> getCommunityList(@RequestParam(required = false) String city,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Community> communities = communityService.getCommunityList(city, keyword, pageNum, pageSize);
        return Result.success(communities);
    }

    /**
     * 获取小区详情（公开接口）
     */
    @GetMapping("/{communityId}")
    public Result<Community> getCommunityDetail(@PathVariable Long communityId) {
        Community community = communityService.getCommunityDetail(communityId);
        return Result.success(community);
    }

    /**
     * 搜索小区（公开接口）
     */
    @GetMapping("/search")
    public Result<List<Community>> searchCommunities(@RequestParam String keyword) {
        List<Community> communities = communityService.searchCommunities(keyword);
        return Result.success(communities);
    }

    /**
     * 根据城市获取小区列表（公开接口）
     */
    @GetMapping("/city/{city}")
    public Result<List<Community>> getCommunitiesByCity(@PathVariable String city) {
        List<Community> communities = communityService.getCommunitiesByCity(city);
        return Result.success(communities);
    }

    /**
     * 获取热门小区列表（公开接口）
     */
    @GetMapping("/popular")
    public Result<List<Community>> getPopularCommunities(@RequestParam(defaultValue = "10") Integer limit) {
        List<Community> communities = communityService.getPopularCommunities(limit);
        return Result.success(communities);
    }

    /**
     * 获取小区配套设施
     */
    @GetMapping("/{communityId}/facilities")
    public Result<List<CommunityFacility>> getCommunityFacilities(@PathVariable Long communityId,
                                                                 @RequestParam(required = false) String facilityType) {
        List<CommunityFacility> facilities = communityService.getCommunityFacilities(communityId, facilityType);
        return Result.success(facilities);
    }

    /**
     * 添加小区配套设施
     */
    @PostMapping("/{communityId}/facilities")
    public Result<CommunityFacility> addCommunityFacility(@PathVariable Long communityId,
                                                         @RequestBody CommunityFacility facility) {
        Long creatorId = StpUtil.getLoginIdAsLong();
        CommunityFacility createdFacility = communityService.addCommunityFacility(communityId, facility, creatorId);
        return Result.success("配套设施添加成功", createdFacility);
    }

    /**
     * 更新小区配套设施
     */
    @PutMapping("/facilities/{facilityId}")
    public Result<CommunityFacility> updateCommunityFacility(@PathVariable Long facilityId,
                                                           @RequestBody CommunityFacility facility) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityFacility updatedFacility = communityService.updateCommunityFacility(facilityId, facility, userId);
        return Result.success("配套设施更新成功", updatedFacility);
    }

    /**
     * 删除小区配套设施
     */
    @DeleteMapping("/facilities/{facilityId}")
    public Result<Void> deleteCommunityFacility(@PathVariable Long facilityId) {
        Long userId = StpUtil.getLoginIdAsLong();
        communityService.deleteCommunityFacility(facilityId, userId);
        return Result.success("配套设施删除成功");
    }

    /**
     * 检查用户是否可以管理小区配套设施
     */
    @GetMapping("/{communityId}/can-manage-facilities")
    public Result<Boolean> canManageFacilities(@PathVariable Long communityId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean canManage = communityService.canManageFacilities(userId, communityId);
        return Result.success(canManage);
    }

    /**
     * 反馈配套信息错误
     */
    @PostMapping("/{communityId}/facilities/feedback")
    public Result<Void> feedbackFacilityError(@PathVariable Long communityId,
                                              @RequestParam String content,
                                              @RequestParam(required = false) Long facilityId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 创建反馈记录
            FacilityFeedback feedback = new FacilityFeedback();
            feedback.setCommunityId(communityId);
            // 如果facilityId无效或不存在，设为null避免外键约束失败
            if (facilityId != null && facilityId > 0) {
                feedback.setFacilityId(facilityId);
            }
            feedback.setUserId(userId);
            feedback.setContent(content);
            feedback.setStatus("pending");
            
            // 保存到数据库
            facilityFeedbackMapper.insert(feedback);
            
            log.info("用户{}提交配套反馈，小区ID：{}，设施ID：{}，反馈内容：{}", userId, communityId, facilityId, content);
            
            return Result.success("反馈提交成功，小区管理员将尽快处理");
        } catch (Exception e) {
            log.error("提交配套反馈失败: communityId={}, facilityId={}", communityId, facilityId, e);
            return Result.error("提交反馈失败，请稍后重试");
        }
    }
}
