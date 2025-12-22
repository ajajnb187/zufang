package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.Map;
import org.example.rentingmanagement.dto.HouseDetailVO;
import org.example.rentingmanagement.dto.HousePublishRequest;
import org.example.rentingmanagement.dto.HouseSearchRequest;
import org.example.rentingmanagement.entity.House;

/**
 * 房源管理服务接口
 */
public interface HouseService {

    /**
     * 发布房源
     */
    House publishHouse(HousePublishRequest request, Long landlordId);

    /**
     * 更新房源信息
     */
    House updateHouse(Long houseId, HousePublishRequest request, Long landlordId);

    /**
     * 删除房源
     */
    boolean deleteHouse(Long houseId, Long landlordId);

    /**
     * 上架房源
     */
    boolean onlineHouse(Long houseId, Long landlordId);

    /**
     * 下架房源
     */
    boolean offlineHouse(Long houseId, Long landlordId);

    /**
     * 搜索房源列表
     */
    IPage<House> searchHouses(HouseSearchRequest request);

    /**
     * 获取房源详情（包含房东信息）
     */
    HouseDetailVO getHouseDetail(Long houseId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long houseId);

    /**
     * 收藏房源
     */
    boolean favoriteHouse(Long houseId, Long userId);

    /**
     * 取消收藏房源
     */
    boolean unfavoriteHouse(Long houseId, Long userId);

    /**
     * 获取用户收藏的房源列表
     */
    List<House> getUserFavoriteHouses(Long userId);

    /**
     * 获取房东的房源列表
     */
    IPage<House> getLandlordHouses(Long landlordId, String status, Integer pageNum, Integer pageSize);

    /**
     * 管理员审核房源
     */
    boolean auditHouse(Long houseId, Long auditorId, boolean approved, String opinion);

    /**
     * 获取待审核房源列表
     */
    IPage<House> getPendingAuditHouses(Long communityId, Integer pageNum, Integer pageSize);

    /**
     * 房源举报
     */
    boolean reportHouse(Long houseId, Long reporterId, String reasonType, String reasonDetail, List<String> evidenceImages);

    /**
     * 检查用户是否可以发布房源（需要小区认证）
     */
    boolean canPublishHouse(Long userId, Long communityId);

    /**
     * 根据ID获取房源信息
     */
    House getHouseById(Long houseId);

    /**
     * 房东更新房源状态
     */
    void updateHouseStatus(Long houseId, Long landlordId, String status);

    /**
     * 获取房东房源统计信息
     */
    Map<String, Object> getLandlordHouseStatistics(Long landlordId);
}
