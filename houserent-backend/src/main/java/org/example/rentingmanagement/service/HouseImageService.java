package org.example.rentingmanagement.service;

import org.example.rentingmanagement.entity.HouseImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 房源图片服务接口
 */
public interface HouseImageService {

    /**
     * 上传房源图片
     * @param houseId 房源ID
     * @param files 图片文件列表
     * @return 上传成功的图片列表
     */
    List<HouseImage> uploadHouseImages(Long houseId, List<MultipartFile> files);

    /**
     * 根据房源ID获取图片列表
     * @param houseId 房源ID
     * @return 图片列表
     */
    List<HouseImage> getImagesByHouseId(Long houseId);

    /**
     * 删除房源图片
     * @param imageId 图片ID
     * @return 是否删除成功
     */
    boolean deleteImage(Long imageId);

    /**
     * 批量删除房源图片
     * @param imageIds 图片ID列表
     * @return 是否删除成功
     */
    boolean deleteImages(List<Long> imageIds);

    /**
     * 设置封面图
     * @param houseId 房源ID
     * @param imageId 图片ID
     * @return 是否设置成功
     */
    boolean setCoverImage(Long houseId, Long imageId);

    /**
     * 更新图片排序
     * @param imageId 图片ID
     * @param sortOrder 排序顺序
     * @return 是否更新成功
     */
    boolean updateSortOrder(Long imageId, Integer sortOrder);

    /**
     * 根据房源ID删除所有图片
     * @param houseId 房源ID
     * @return 是否删除成功
     */
    boolean deleteAllImagesByHouseId(Long houseId);
}
