package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.HouseDetailVO;
import org.example.rentingmanagement.dto.HousePublishRequest;
import org.example.rentingmanagement.dto.HouseSearchRequest;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.entity.HouseImage;
import org.example.rentingmanagement.service.HouseService;
import org.example.rentingmanagement.service.HouseImageService;
import org.example.rentingmanagement.util.ImageUrlUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 房源管理控制器
 */
@RestController
@RequestMapping("/api/houses")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;
    private final HouseImageService houseImageService;
    private final ImageUrlUtil imageUrlUtil;

    /**
     * 发布房源
     */
    @PostMapping("/publish")
    @SaCheckRole("landlord")
    public Result<House> publishHouse(@RequestBody @Valid HousePublishRequest request) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        House house = houseService.publishHouse(request, landlordId);
        return Result.success("房源发布成功", house);
    }

    /**
     * 更新房源信息
     */
    @PutMapping("/{houseId}")
    @SaCheckRole("landlord")
    public Result<House> updateHouse(@PathVariable Long houseId,
                                     @RequestBody @Valid HousePublishRequest request) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        House house = houseService.updateHouse(houseId, request, landlordId);
        return Result.success("房源更新成功", house);
    }

    /**
     * 删除房源
     */
    @DeleteMapping("/{houseId}")
    @SaCheckRole("landlord")
    public Result<Void> deleteHouse(@PathVariable Long houseId) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        houseService.deleteHouse(houseId, landlordId);
        return Result.success("房源删除成功");
    }

    /**
     * 上架房源
     */
    @PostMapping("/{houseId}/online")
    @SaCheckRole("landlord")
    public Result<Void> onlineHouse(@PathVariable Long houseId) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        houseService.onlineHouse(houseId, landlordId);
        return Result.success("房源上架成功");
    }

    /**
     * 下架房源
     */
    @PostMapping("/{houseId}/offline")
    @SaCheckRole("landlord")
    public Result<Void> offlineHouse(@PathVariable Long houseId) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        houseService.offlineHouse(houseId, landlordId);
        return Result.success("房源下架成功");
    }

    /**
     * 搜索房源列表（公开接口，游客可访问）
     */
    @PostMapping("/search")
    public Result<IPage<House>> searchHouses(@RequestBody HouseSearchRequest request) {
        IPage<House> houses = houseService.searchHouses(request);
        return Result.success(houses);
    }

    /**
     * 获取房源详情（公开接口，游客可访问）
     * 返回包含房东信息、小区名称、收藏状态的完整房源详情
     */
    @GetMapping("/{houseId}/detail")
    public Result<HouseDetailVO> getHouseDetail(@PathVariable Long houseId) {
        // 增加浏览次数
        houseService.incrementViewCount(houseId);

        HouseDetailVO houseDetail = houseService.getHouseDetail(houseId);
        return Result.success(houseDetail);
    }

    /**
     * 收藏房源
     */
    @PostMapping("/{houseId}/favorite")
    public Result<Void> favoriteHouse(@PathVariable Long houseId) {
        Long userId = StpUtil.getLoginIdAsLong();
        houseService.favoriteHouse(houseId, userId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏房源
     */
    @DeleteMapping("/{houseId}/favorite")
    public Result<Void> unfavoriteHouse(@PathVariable Long houseId) {
        Long userId = StpUtil.getLoginIdAsLong();
        houseService.unfavoriteHouse(houseId, userId);
        return Result.success("取消收藏成功");
    }

    /**
     * 获取用户收藏的房源列表
     */
    @GetMapping("/favorites")
    public Result<List<House>> getUserFavoriteHouses() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<House> houses = houseService.getUserFavoriteHouses(userId);
        return Result.success(houses);
    }

    /**
     * 获取房东的房源列表
     */
    @GetMapping("/landlord")
    @SaCheckRole("landlord")
    public Result<IPage<House>> getLandlordHouses(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String auditStatus,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        IPage<House> houses = ((org.example.rentingmanagement.service.impl.HouseServiceImpl) houseService)
                .getLandlordHouses(landlordId, status, auditStatus, pageNum, pageSize);
        return Result.success(houses);
    }

    /**
     * 房东更新房源状态
     */
    @PutMapping("/landlord/{houseId}/status")
    @SaCheckRole("landlord")
    public Result<Void> updateHouseStatus(@PathVariable Long houseId,
                                          @RequestParam String status) {
        Long landlordId = StpUtil.getLoginIdAsLong();
        houseService.updateHouseStatus(houseId, landlordId, status);
        return Result.success();
    }

    /**
     * 举报房源
     */
    @PostMapping("/{houseId}/report")
    public Result<Void> reportHouse(@PathVariable Long houseId,
                                    @RequestParam String reasonType,
                                    @RequestParam String reasonDetail,
                                    @RequestParam(required = false) List<String> evidenceImages) {
        Long reporterId = StpUtil.getLoginIdAsLong();
        houseService.reportHouse(houseId, reporterId, reasonType, reasonDetail, evidenceImages);
        return Result.success("举报成功");
    }

    /**
     * 管理员审核房源
     */
    @PostMapping("/{houseId}/audit")
    @SaCheckRole("admin")
    public Result<Void> auditHouse(@PathVariable Long houseId,
                                   @RequestParam boolean approved,
                                   @RequestParam(required = false) String opinion) {
        Long auditorId = StpUtil.getLoginIdAsLong();
        houseService.auditHouse(houseId, auditorId, approved, opinion);
        return Result.success("审核完成");
    }

    /**
     * 获取待审核房源列表
     */
    @GetMapping("/pending-audit")
    @SaCheckRole("admin")
    public Result<IPage<House>> getPendingAuditHouses(@RequestParam(required = false) Long communityId,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<House> houses = houseService.getPendingAuditHouses(communityId, pageNum, pageSize);
        return Result.success(houses);
    }

    // ==================== 房源图片管理接口 ====================

    /**
     * 上传房源图片
     */
    @PostMapping("/{houseId}/images")
    @SaCheckRole("landlord")
    public Result<List<HouseImage>> uploadHouseImages(@PathVariable Long houseId,
                                                     @RequestParam("files") List<MultipartFile> files) {
        // 验证房源所有权
        Long landlordId = StpUtil.getLoginIdAsLong();
        House house = houseService.getHouseById(houseId);
        if (house == null || !house.getLandlordId().equals(landlordId)) {
            return Result.error("无权限操作此房源");
        }

        List<HouseImage> images = houseImageService.uploadHouseImages(houseId, files);
        // 转换为Base64
        images.forEach(img -> img.setImageUrl(imageUrlUtil.toBase64DataUrl(img.getImageUrl())));
        return Result.success("图片上传成功", images);
    }

    /**
     * 获取房源图片列表
     */
    @GetMapping("/{houseId}/images")
    public Result<List<HouseImage>> getHouseImages(@PathVariable Long houseId) {
        List<HouseImage> images = houseImageService.getImagesByHouseId(houseId);
        // 转换为Base64
        images.forEach(img -> img.setImageUrl(imageUrlUtil.toBase64DataUrl(img.getImageUrl())));
        return Result.success(images);
    }

    /**
     * 删除房源图片
     */
    @DeleteMapping("/images/{imageId}")
    @SaCheckRole("landlord")
    public Result<Void> deleteHouseImage(@PathVariable Long imageId) {
        // 验证图片所有权
        Long landlordId = StpUtil.getLoginIdAsLong();
        // 这里需要通过图片ID获取房源信息来验证权限
        boolean success = houseImageService.deleteImage(imageId);
        if (success) {
            return Result.success("图片删除成功");
        } else {
            return Result.error("图片删除失败");
        }
    }

    /**
     * 设置封面图
     */
    @PostMapping("/{houseId}/images/{imageId}/cover")
    @SaCheckRole("landlord")
    public Result<Void> setCoverImage(@PathVariable Long houseId, @PathVariable Long imageId) {
        // 验证房源所有权
        Long landlordId = StpUtil.getLoginIdAsLong();
        House house = houseService.getHouseById(houseId);
        if (house == null || !house.getLandlordId().equals(landlordId)) {
            return Result.error("无权限操作此房源");
        }

        boolean success = houseImageService.setCoverImage(houseId, imageId);
        if (success) {
            return Result.success("封面图设置成功");
        } else {
            return Result.error("封面图设置失败");
        }
    }

    /**
     * 更新图片排序
     */
    @PutMapping("/images/{imageId}/sort")
    @SaCheckRole("landlord")
    public Result<Void> updateImageSort(@PathVariable Long imageId, @RequestParam Integer sortOrder) {
        boolean success = houseImageService.updateSortOrder(imageId, sortOrder);
        if (success) {
            return Result.success("排序更新成功");
        } else {
            return Result.error("排序更新失败");
        }
    }
}