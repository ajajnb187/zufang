package org.example.rentingmanagement.dto;

import lombok.Data;
import org.example.rentingmanagement.entity.House;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源详情VO - 包含房源信息和房东信息
 */
@Data
public class HouseDetailVO {

    // ========== 房源基本信息 ==========
    private Long houseId;
    private Long landlordId;
    private Long communityId;
    private String title;
    private String houseType;
    private BigDecimal area;
    private String floor;
    private String totalFloor;
    private String orientation;
    private String decoration;
    private BigDecimal rentPrice;
    private String paymentMethod;
    private String rentPeriod;
    private String description;
    private String images;
    private String contactPhone;
    private String facilities;
    private String auditStatus;
    private String publishStatus;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ========== 小区信息 ==========
    private String communityName;
    
    // ========== 房东信息 ==========
    private LandlordInfo landlord;
    
    // ========== 当前用户相关 ==========
    private Boolean isFavorited;

    /**
     * 房东信息内部类
     */
    @Data
    public static class LandlordInfo {
        private Long userId;
        private String nickname;
        private String avatarUrl;
        private String phone;
        private BigDecimal creditScore;
    }

    /**
     * 从House实体构建VO
     */
    public static HouseDetailVO fromHouse(House house) {
        HouseDetailVO vo = new HouseDetailVO();
        vo.setHouseId(house.getHouseId());
        vo.setLandlordId(house.getLandlordId());
        vo.setCommunityId(house.getCommunityId());
        vo.setTitle(house.getTitle());
        vo.setHouseType(house.getHouseType());
        vo.setArea(house.getArea());
        vo.setFloor(house.getFloor());
        vo.setTotalFloor(house.getTotalFloor());
        vo.setOrientation(house.getOrientation());
        vo.setDecoration(house.getDecoration());
        vo.setRentPrice(house.getRentPrice());
        vo.setPaymentMethod(house.getPaymentMethod());
        vo.setRentPeriod(house.getRentPeriod());
        vo.setDescription(house.getDescription());
        vo.setImages(house.getImages());
        vo.setContactPhone(house.getContactPhone());
        vo.setFacilities(house.getFacilities());
        vo.setAuditStatus(house.getAuditStatus());
        vo.setPublishStatus(house.getPublishStatus());
        vo.setViewCount(house.getViewCount());
        vo.setFavoriteCount(house.getFavoriteCount());
        vo.setCreatedAt(house.getCreatedAt());
        vo.setUpdatedAt(house.getUpdatedAt());
        return vo;
    }
}
