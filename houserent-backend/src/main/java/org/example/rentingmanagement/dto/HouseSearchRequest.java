package org.example.rentingmanagement.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 房源搜索请求DTO
 */
@Data
public class HouseSearchRequest {

    /**
     * 关键字搜索（小区名/标题）
     */
    private String keyword;

    /**
     * 小区ID
     */
    private Long communityId;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 租赁期限筛选：短期, 长期
     */
    private String rentPeriod;

    /**
     * 户型（如：1室、2室、3室）
     */
    private String houseType;

    /**
     * 面积范围（如：0-50, 50-80）
     */
    private String areaRange;

    /**
     * 价格范围（如：0-1000, 1000-2000）
     */
    private String priceRange;

    /**
     * 朝向
     */
    private String orientation;

    /**
     * 装修情况
     */
    private String decoration;

    /**
     * 最小租金
     */
    private BigDecimal minPrice;

    /**
     * 最大租金
     */
    private BigDecimal maxPrice;

    /**
     * 最小面积
     */
    private BigDecimal minArea;

    /**
     * 最大面积
     */
    private BigDecimal maxArea;

    /**
     * 排序方式: price_asc, price_desc, area_asc, area_desc, time_desc
     */
    private String sortBy = "time_desc";

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
