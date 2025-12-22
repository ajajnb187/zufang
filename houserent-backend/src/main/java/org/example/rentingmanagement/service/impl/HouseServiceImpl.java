package org.example.rentingmanagement.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.HouseDetailVO;
import org.example.rentingmanagement.dto.HousePublishRequest;
import org.example.rentingmanagement.dto.HouseSearchRequest;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.entity.HouseFavorite;
import org.example.rentingmanagement.entity.HouseImage;
import org.example.rentingmanagement.entity.Report;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.CommunityMapper;
import org.example.rentingmanagement.mapper.CommunityVerificationMapper;
import org.example.rentingmanagement.mapper.HouseFavoriteMapper;
import org.example.rentingmanagement.mapper.HouseMapper;
import org.example.rentingmanagement.mapper.ReportMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.HouseService;
import org.example.rentingmanagement.service.HouseImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房源管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    private final HouseFavoriteMapper houseFavoriteMapper;
    private final ReportMapper reportMapper;
    private final CommunityVerificationMapper communityVerificationMapper;
    private final HouseImageService houseImageService;
    private final UserMapper userMapper;
    private final CommunityMapper communityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public House publishHouse(HousePublishRequest request, Long landlordId) {
        try {
            // 检查用户是否可以在该小区发布房源
            if (!canPublishHouse(landlordId, request.getCommunityId())) {
                throw new BusinessException(ResultCode.COMMUNITY_NOT_VERIFIED, "请先完成小区身份认证");
            }

            // 创建房源实体
            House house = new House();
            house.setLandlordId(landlordId);
            house.setCommunityId(request.getCommunityId());
            house.setTitle(request.getTitle());
            house.setHouseType(request.getHouseType());
            house.setArea(request.getArea());
            house.setFloor(request.getFloor());
            house.setTotalFloor(request.getTotalFloor());
            house.setOrientation(request.getOrientation());
            house.setDecoration(request.getDecoration());
            house.setRentPrice(request.getRentPrice());
            house.setPaymentMethod(request.getPaymentMethod());
            house.setRentPeriod(request.getRentPeriod());
            house.setDescription(request.getDescription());
            house.setContactPhone(request.getContactPhone());
            
            // 转换设施为JSON格式，图片不存储在houses表中
            house.setFacilities(request.getFacilities() != null ? JSON.toJSONString(request.getFacilities()) : null);
            
            // 设置初始状态
            house.setAuditStatus("pending");
            house.setPublishStatus("offline");
            house.setViewCount(0);
            house.setFavoriteCount(0);

            // 保存房源
            this.save(house);

            log.info("房源发布成功: houseId={}, landlordId={}", house.getHouseId(), landlordId);
            return house;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发布房源失败: {}", e.getMessage(), e);
            throw new BusinessException("发布房源失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public House updateHouse(Long houseId, HousePublishRequest request, Long landlordId) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证房源所有者
            if (!house.getLandlordId().equals(landlordId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此房源");
            }

            // 检查房源状态，已出租的房源不能修改
            if ("rented".equals(house.getPublishStatus())) {
                throw new BusinessException(ResultCode.HOUSE_OFFLINE, "已出租的房源无法修改");
            }

            // 更新房源信息
            house.setTitle(request.getTitle());
            house.setHouseType(request.getHouseType());
            house.setArea(request.getArea());
            house.setFloor(request.getFloor());
            house.setTotalFloor(request.getTotalFloor());
            house.setOrientation(request.getOrientation());
            house.setDecoration(request.getDecoration());
            house.setRentPrice(request.getRentPrice());
            house.setPaymentMethod(request.getPaymentMethod());
            house.setRentPeriod(request.getRentPeriod());
            house.setDescription(request.getDescription());
            house.setContactPhone(request.getContactPhone());
            house.setFacilities(request.getFacilities() != null ? JSON.toJSONString(request.getFacilities()) : null);

            // 如果房源之前已审核通过且在线，修改后保持在线状态，但需要重新审核
            if ("approved".equals(house.getAuditStatus()) && "online".equals(house.getPublishStatus())) {
                // 保持在线状态，但标记为需要重新审核
                house.setAuditStatus("pending");
                house.setAuditorId(null);
                house.setAuditOpinion(null);
                house.setAuditTime(null);
                // 保持在线状态，让房东继续展示房源
            } else {
                // 其他情况重新提交审核
                house.setAuditStatus("pending");
                house.setAuditorId(null);
                house.setAuditOpinion(null);
                house.setAuditTime(null);
                house.setPublishStatus("offline");
            }

            this.updateById(house);

            log.info("房源更新成功: houseId={}, landlordId={}", houseId, landlordId);
            return house;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新房源失败: {}", e.getMessage(), e);
            throw new BusinessException("更新房源失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteHouse(Long houseId, Long landlordId) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证房源所有者
            if (!house.getLandlordId().equals(landlordId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此房源");
            }

            // 检查是否有进行中的合同
            // TODO: 这里可以添加合同检查逻辑

            // 删除房源
            this.removeById(houseId);

            log.info("房源删除成功: houseId={}, landlordId={}", houseId, landlordId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除房源失败: {}", e.getMessage(), e);
            throw new BusinessException("删除房源失败");
        }
    }

    @Override
    public boolean onlineHouse(Long houseId, Long landlordId) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证房源所有者
            if (!house.getLandlordId().equals(landlordId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此房源");
            }

            // 检查审核状态
            if (!"approved".equals(house.getAuditStatus())) {
                throw new BusinessException("房源未通过审核，无法上架");
            }

            // 上架房源
            house.setPublishStatus("online");
            this.updateById(house);

            log.info("房源上架成功: houseId={}", houseId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("房源上架失败: {}", e.getMessage(), e);
            throw new BusinessException("房源上架失败");
        }
    }

    @Override
    public boolean offlineHouse(Long houseId, Long landlordId) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证房源所有者
            if (!house.getLandlordId().equals(landlordId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此房源");
            }

            // 下架房源
            house.setPublishStatus("offline");
            this.updateById(house);

            log.info("房源下架成功: houseId={}", houseId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("房源下架失败: {}", e.getMessage(), e);
            throw new BusinessException("房源下架失败");
        }
    }

    @Override
    public IPage<House> searchHouses(HouseSearchRequest request) {
        try {
            Page<House> page = new Page<>(request.getPageNum(), request.getPageSize());

            // 解析价格范围
            Double minPrice = null, maxPrice = null;
            if (request.getPriceRange() != null && !request.getPriceRange().isEmpty()) {
                String[] priceRange = request.getPriceRange().split("-");
                if (priceRange.length == 2) {
                    minPrice = Double.parseDouble(priceRange[0]);
                    maxPrice = Double.parseDouble(priceRange[1]);
                }
            } else {
                minPrice = request.getMinPrice() != null ? request.getMinPrice().doubleValue() : null;
                maxPrice = request.getMaxPrice() != null ? request.getMaxPrice().doubleValue() : null;
            }

            // 解析面积范围
            Double minArea = null, maxArea = null;
            if (request.getAreaRange() != null && !request.getAreaRange().isEmpty()) {
                String[] areaRange = request.getAreaRange().split("-");
                if (areaRange.length == 2) {
                    minArea = Double.parseDouble(areaRange[0]);
                    maxArea = Double.parseDouble(areaRange[1]);
                }
            } else {
                minArea = request.getMinArea() != null ? request.getMinArea().doubleValue() : null;
                maxArea = request.getMaxArea() != null ? request.getMaxArea().doubleValue() : null;
            }

            // 处理户型筛选（前端传"1室"、"2室"等，需要转换为数据库格式）
            String houseType = request.getHouseType();
            if (houseType != null && !houseType.isEmpty()) {
                // 提取数字部分用于模糊匹配
                houseType = houseType.replace("室", "").replace("及以上", "");
            }

            // 使用HouseMapper的自定义查询方法（支持地区筛选）
            IPage<House> result = baseMapper.findHousesWithConditions(
                    page,
                    request.getCommunityId(),
                    request.getProvince(),
                    request.getCity(),
                    request.getDistrict(),
                    houseType,
                    minPrice,
                    maxPrice,
                    minArea,
                    maxArea
            );

            // 关键字过滤（在内存中进行，因为需要关联小区名称）
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String keyword = request.getKeyword().toLowerCase();
                result.setRecords(result.getRecords().stream()
                        .filter(house -> {
                            boolean match = house.getTitle() != null && house.getTitle().toLowerCase().contains(keyword);
                            // TODO: 可以扩展小区名称匹配
                            return match;
                        })
                        .collect(Collectors.toList()));
            }

            // 为每个房源加载封面图片
            for (House house : result.getRecords()) {
                List<HouseImage> images = houseImageService.getImagesByHouseId(house.getHouseId());
                if (!images.isEmpty()) {
                    // 优先显示封面图，如果没有封面则显示第一张
                    HouseImage coverImage = images.stream()
                            .filter(img -> img.getIsCover() != null && img.getIsCover())
                            .findFirst()
                            .orElse(images.get(0));
                    house.setImages(JSON.toJSONString(List.of(coverImage.getImageUrl())));
                } else {
                    house.setImages(JSON.toJSONString(List.of()));
                }
            }

            return result;

        } catch (Exception e) {
            log.error("搜索房源失败: {}", e.getMessage(), e);
            throw new BusinessException("搜索房源失败");
        }
    }

    @Override
    public HouseDetailVO getHouseDetail(Long houseId) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 检查房源是否可见（游客只能看到已审核通过的在线房源）
            Long currentUserId = null;
            try {
                currentUserId = StpUtil.getLoginIdAsLong();
            } catch (Exception ignored) {
                // 游客访问
            }

            if (currentUserId == null || !currentUserId.equals(house.getLandlordId())) {
                // 非房东访问，检查房源状态
                if (!"approved".equals(house.getAuditStatus()) || !"online".equals(house.getPublishStatus())) {
                    throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在或已下架");
                }
            }

            // 加载房源图片
            List<HouseImage> images = houseImageService.getImagesByHouseId(houseId);
            house.setImages(JSON.toJSONString(images.stream().map(HouseImage::getImageUrl).collect(Collectors.toList())));

            // 构建返回VO
            HouseDetailVO vo = HouseDetailVO.fromHouse(house);
            
            // 加载房东信息
            User landlord = userMapper.selectById(house.getLandlordId());
            if (landlord != null) {
                HouseDetailVO.LandlordInfo landlordInfo = new HouseDetailVO.LandlordInfo();
                landlordInfo.setUserId(landlord.getUserId());
                landlordInfo.setNickname(landlord.getNickname());
                landlordInfo.setAvatarUrl(landlord.getAvatarUrl());
                landlordInfo.setPhone(landlord.getPhone());
                landlordInfo.setCreditScore(landlord.getCreditScore());
                vo.setLandlord(landlordInfo);
            }
            
            // 加载小区名称
            Community community = communityMapper.selectById(house.getCommunityId());
            if (community != null) {
                vo.setCommunityName(community.getCommunityName());
            }
            
            // 检查当前用户是否已收藏
            if (currentUserId != null) {
                boolean isFavorited = houseFavoriteMapper.isFavorited(currentUserId, houseId);
                vo.setIsFavorited(isFavorited);
            } else {
                vo.setIsFavorited(false);
            }

            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取房源详情失败: {}", e.getMessage(), e);
            throw new BusinessException("获取房源详情失败");
        }
    }

    @Override
    public void incrementViewCount(Long houseId) {
        try {
            baseMapper.incrementViewCount(houseId);
        } catch (Exception e) {
            log.error("增加浏览次数失败: houseId={}", houseId, e);
            // 浏览次数更新失败不抛出异常，不影响正常流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean favoriteHouse(Long houseId, Long userId) {
        try {
            // 检查房源是否存在
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 检查是否已收藏
            if (houseFavoriteMapper.isFavorited(userId, houseId)) {
                throw new BusinessException("已收藏该房源");
            }

            // 创建收藏记录
            HouseFavorite favorite = new HouseFavorite();
            favorite.setUserId(userId);
            favorite.setHouseId(houseId);
            houseFavoriteMapper.insert(favorite);

            // 更新房源收藏数
            int favoriteCount = houseFavoriteMapper.countByHouseId(houseId);
            baseMapper.updateFavoriteCount(houseId, favoriteCount);

            log.info("房源收藏成功: houseId={}, userId={}", houseId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("收藏房源失败: {}", e.getMessage(), e);
            throw new BusinessException("收藏房源失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfavoriteHouse(Long houseId, Long userId) {
        try {
            // 删除收藏记录
            LambdaQueryWrapper<HouseFavorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HouseFavorite::getUserId, userId)
                   .eq(HouseFavorite::getHouseId, houseId);
            houseFavoriteMapper.delete(wrapper);

            // 更新房源收藏数
            int favoriteCount = houseFavoriteMapper.countByHouseId(houseId);
            baseMapper.updateFavoriteCount(houseId, favoriteCount);

            log.info("取消房源收藏成功: houseId={}, userId={}", houseId, userId);
            return true;

        } catch (Exception e) {
            log.error("取消收藏房源失败: {}", e.getMessage(), e);
            throw new BusinessException("取消收藏房源失败");
        }
    }

    @Override
    public List<House> getUserFavoriteHouses(Long userId) {
        try {
            return houseFavoriteMapper.findUserFavorites(userId)
                    .stream()
                    .map(favorite -> this.getById(favorite.getHouseId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户收藏房源失败: {}", e.getMessage(), e);
            throw new BusinessException("获取收藏房源失败");
        }
    }

    @Override
    public IPage<House> getLandlordHouses(Long landlordId, String status, Integer pageNum, Integer pageSize) {
        // 调用重载方法，auditStatus为null
        return getLandlordHouses(landlordId, status, null, pageNum, pageSize);
    }

    /**
     * 获取房东房源列表（支持审核状态过滤）
     */
    public IPage<House> getLandlordHouses(Long landlordId, String status, String auditStatus, Integer pageNum, Integer pageSize) {
        try {
            Page<House> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(House::getLandlordId, landlordId);
            
            // 如果status不为null，添加发布状态过滤
            if (status != null && !"null".equals(status) && !status.trim().isEmpty()) {
                wrapper.eq(House::getPublishStatus, status);
            }
            
            // 如果auditStatus不为null，添加审核状态过滤
            if (auditStatus != null && !"null".equals(auditStatus) && !auditStatus.trim().isEmpty()) {
                wrapper.eq(House::getAuditStatus, auditStatus);
            }
            
            wrapper.orderByDesc(House::getCreatedAt);
            IPage<House> result = this.page(page, wrapper);
            
            // 为每个房源加载封面图片
            for (House house : result.getRecords()) {
                List<HouseImage> images = houseImageService.getImagesByHouseId(house.getHouseId());
                if (!images.isEmpty()) {
                    // 优先显示封面图，如果没有封面则显示第一张
                    HouseImage coverImage = images.stream()
                            .filter(img -> img.getIsCover() != null && img.getIsCover())
                            .findFirst()
                            .orElse(images.get(0));
                    house.setImages(JSON.toJSONString(List.of(coverImage.getImageUrl())));
                } else {
                    house.setImages(JSON.toJSONString(List.of()));
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取房东房源列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取房源列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditHouse(Long houseId, Long auditorId, boolean approved, String opinion) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            if (!"pending".equals(house.getAuditStatus())) {
                throw new BusinessException("房源状态错误，无法审核");
            }

            // 更新审核信息
            house.setAuditorId(auditorId);
            house.setAuditOpinion(opinion);
            house.setAuditTime(java.time.LocalDateTime.now());

            if (approved) {
                house.setAuditStatus("approved");
                // 审核通过后可以选择上架
                house.setPublishStatus("online");
            } else {
                house.setAuditStatus("rejected");
                house.setPublishStatus("offline");
            }

            this.updateById(house);

            log.info("房源审核完成: houseId={}, approved={}", houseId, approved);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("房源审核失败: {}", e.getMessage(), e);
            throw new BusinessException("房源审核失败");
        }
    }

    @Override
    public IPage<House> getPendingAuditHouses(Long communityId, Integer pageNum, Integer pageSize) {
        try {
            Page<House> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(House::getAuditStatus, "pending")
                   .eq(communityId != null, House::getCommunityId, communityId)
                   .orderByAsc(House::getCreatedAt);
            return this.page(page, wrapper);
        } catch (Exception e) {
            log.error("获取待审核房源列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取待审核房源失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reportHouse(Long houseId, Long reporterId, String reasonType, String reasonDetail, List<String> evidenceImages) {
        try {
            // 检查房源是否存在
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 创建举报记录
            Report report = new Report();
            report.setReporterId(reporterId);
            report.setReportType("house");
            report.setTargetId(houseId);
            report.setReasonType(reasonType);
            report.setReasonDetail(reasonDetail);
            report.setEvidenceImages(evidenceImages != null ? JSON.toJSONString(evidenceImages) : null);
            report.setStatus("pending");

            reportMapper.insert(report);

            log.info("房源举报成功: houseId={}, reporterId={}", houseId, reporterId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("举报房源失败: {}", e.getMessage(), e);
            throw new BusinessException("举报房源失败");
        }
    }

    @Override
    public boolean canPublishHouse(Long userId, Long communityId) {
        try {
            return communityVerificationMapper.isUserVerified(userId, communityId);
        } catch (Exception e) {
            log.error("检查发布权限失败: userId={}, communityId={}", userId, communityId, e);
            return false;
        }
    }

    @Override
    public House getHouseById(Long houseId) {
        return this.getById(houseId);
    }

    @Override
    public void updateHouseStatus(Long houseId, Long landlordId, String status) {
        try {
            House house = this.getById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证房源所有权
            if (!house.getLandlordId().equals(landlordId)) {
                throw new BusinessException(ResultCode.PERMISSION_DENIED, "无权限操作此房源");
            }

            // 验证状态转换的合法性
            if ("online".equals(status)) {
                // 上架需要审核通过
                if (!"approved".equals(house.getAuditStatus())) {
                    throw new BusinessException(ResultCode.OPERATION_FAILED, "房源未通过审核，无法上架");
                }
                house.setPublishStatus("online");
            } else if ("offline".equals(status)) {
                // 下架
                house.setPublishStatus("offline");
            } else {
                throw new BusinessException(ResultCode.INVALID_PARAMETER, "无效的状态参数");
            }

            this.updateById(house);
            log.info("房源状态更新成功: houseId={}, status={}", houseId, status);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新房源状态失败: {}", e.getMessage(), e);
            throw new BusinessException("更新房源状态失败");
        }
    }

    @Override
    public Map<String, Object> getLandlordHouseStatistics(Long landlordId) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总房源数
            LambdaQueryWrapper<House> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(House::getLandlordId, landlordId);
            long totalHouses = this.count(totalWrapper);
            statistics.put("totalHouses", (int) totalHouses);
            
            // 在线房源数
            LambdaQueryWrapper<House> onlineWrapper = new LambdaQueryWrapper<>();
            onlineWrapper.eq(House::getLandlordId, landlordId)
                        .eq(House::getPublishStatus, "online");
            long onlineHouses = this.count(onlineWrapper);
            statistics.put("onlineHouses", (int) onlineHouses);
            
            // 离线房源数
            LambdaQueryWrapper<House> offlineWrapper = new LambdaQueryWrapper<>();
            offlineWrapper.eq(House::getLandlordId, landlordId)
                         .eq(House::getPublishStatus, "offline");
            long offlineHouses = this.count(offlineWrapper);
            statistics.put("offlineHouses", (int) offlineHouses);
            
            // 已出租房源数
            LambdaQueryWrapper<House> rentedWrapper = new LambdaQueryWrapper<>();
            rentedWrapper.eq(House::getLandlordId, landlordId)
                        .eq(House::getPublishStatus, "rented");
            long rentedHouses = this.count(rentedWrapper);
            statistics.put("rentedHouses", (int) rentedHouses);
            
            // 空置房源数（在线但未出租）
            long vacantHouses = onlineHouses;
            statistics.put("vacantHouses", (int) vacantHouses);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取房东房源统计失败: landlordId={}", landlordId, e);
            // 返回默认值
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalHouses", 0);
            defaultStats.put("onlineHouses", 0);
            defaultStats.put("offlineHouses", 0);
            defaultStats.put("rentedHouses", 0);
            defaultStats.put("vacantHouses", 0);
            return defaultStats;
        }
    }
}
