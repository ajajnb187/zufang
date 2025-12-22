package org.example.rentingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.entity.HouseImage;
import org.example.rentingmanagement.mapper.HouseImageMapper;
import org.example.rentingmanagement.service.HouseImageService;
import org.example.rentingmanagement.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 房源图片服务实现类
 */
@Slf4j
@Service
public class HouseImageServiceImpl implements HouseImageService {

    @Autowired
    private HouseImageMapper houseImageMapper;

    @Autowired
    private MinioService minioService;

    @Override
    @Transactional
    public List<HouseImage> uploadHouseImages(Long houseId, List<MultipartFile> files) {
        List<HouseImage> uploadedImages = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return uploadedImages;
        }

        // 获取当前房源的图片数量，用于设置排序
        List<HouseImage> existingImages = houseImageMapper.selectList(
                new QueryWrapper<HouseImage>()
                        .eq("house_id", houseId)
                        .orderByAsc("sort_order")
        );
        int currentMaxOrder = existingImages.stream()
                .mapToInt(img -> img.getSortOrder() != null ? img.getSortOrder() : 0)
                .max()
                .orElse(0);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            
            try {
                // 上传文件到MinIO
                String imageUrl = minioService.uploadFile(file, "houses");
                
                // 创建图片记录
                HouseImage houseImage = new HouseImage();
                houseImage.setHouseId(houseId);
                houseImage.setImageUrl(imageUrl);
                houseImage.setImageName(file.getOriginalFilename());
                houseImage.setImageSize(file.getSize());
                houseImage.setImageType(file.getContentType());
                houseImage.setSortOrder(currentMaxOrder + i + 1);
                houseImage.setIsCover(existingImages.isEmpty() && i == 0); // 第一张图片设为封面
                houseImage.setUploadTime(LocalDateTime.now());
                
                // 保存到数据库
                houseImageMapper.insert(houseImage);
                uploadedImages.add(houseImage);
                
                log.info("House image uploaded successfully: houseId={}, imageUrl={}", houseId, imageUrl);
                
            } catch (Exception e) {
                log.error("Failed to upload house image: houseId={}, fileName={}", houseId, file.getOriginalFilename(), e);
                // 继续处理其他文件
            }
        }
        
        return uploadedImages;
    }

    @Override
    public List<HouseImage> getImagesByHouseId(Long houseId) {
        return houseImageMapper.selectList(
                new QueryWrapper<HouseImage>()
                        .eq("house_id", houseId)
                        .orderByAsc("sort_order", "created_at")
        );
    }

    @Override
    @Transactional
    public boolean deleteImage(Long imageId) {
        try {
            // 先获取图片信息
            HouseImage image = houseImageMapper.selectById(imageId);
            if (image == null) {
                return false;
            }
            
            // 从MinIO删除文件
            minioService.deleteFile(image.getImageUrl());
            
            // 从数据库删除记录
            int result = houseImageMapper.deleteById(imageId);
            
            log.info("House image deleted successfully: imageId={}, imageUrl={}", imageId, image.getImageUrl());
            return result > 0;
            
        } catch (Exception e) {
            log.error("Failed to delete house image: imageId={}", imageId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteImages(List<Long> imageIds) {
        boolean allSuccess = true;
        
        for (Long imageId : imageIds) {
            if (!deleteImage(imageId)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }

    @Override
    @Transactional
    public boolean setCoverImage(Long houseId, Long imageId) {
        try {
            // 先取消当前房源的所有封面图
            List<HouseImage> images = houseImageMapper.selectList(
                    new QueryWrapper<HouseImage>()
                            .eq("house_id", houseId)
                            .eq("is_cover", true)
            );
            for (HouseImage image : images) {
                image.setIsCover(false);
                houseImageMapper.updateById(image);
            }
            
            // 设置新的封面图
            HouseImage newCoverImage = houseImageMapper.selectById(imageId);
            if (newCoverImage != null && newCoverImage.getHouseId().equals(houseId)) {
                newCoverImage.setIsCover(true);
                int result = houseImageMapper.updateById(newCoverImage);
                
                log.info("Cover image updated successfully: houseId={}, imageId={}", houseId, imageId);
                return result > 0;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Failed to set cover image: houseId={}, imageId={}", houseId, imageId, e);
            return false;
        }
    }

    @Override
    public boolean updateSortOrder(Long imageId, Integer sortOrder) {
        try {
            HouseImage image = houseImageMapper.selectById(imageId);
            if (image != null) {
                image.setSortOrder(sortOrder);
                int result = houseImageMapper.updateById(image);
                
                log.info("Image sort order updated successfully: imageId={}, sortOrder={}", imageId, sortOrder);
                return result > 0;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Failed to update image sort order: imageId={}, sortOrder={}", imageId, sortOrder, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteAllImagesByHouseId(Long houseId) {
        try {
            // 获取所有图片
            List<HouseImage> images = houseImageMapper.selectList(
                    new QueryWrapper<HouseImage>()
                            .eq("house_id", houseId)
            );
            
            // 从MinIO删除所有文件
            List<String> imageUrls = new ArrayList<>();
            for (HouseImage image : images) {
                imageUrls.add(image.getImageUrl());
            }
            minioService.deleteFiles(imageUrls);
            
            // 从数据库删除所有记录
            int result = houseImageMapper.delete(
                    new QueryWrapper<HouseImage>()
                            .eq("house_id", houseId)
            );
            
            log.info("All house images deleted successfully: houseId={}, count={}", houseId, result);
            return result >= 0;
            
        } catch (Exception e) {
            log.error("Failed to delete all house images: houseId={}", houseId, e);
            return false;
        }
    }
}
