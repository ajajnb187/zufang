package org.example.rentingmanagement.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.config.MinioConfig;
import org.example.rentingmanagement.entity.FileUpload;
import org.example.rentingmanagement.mapper.FileUploadMapper;
import org.example.rentingmanagement.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传服务实现类
 * 基于MinIO 8.5.7最新版本实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl extends ServiceImpl<FileUploadMapper, FileUpload> implements FileUploadService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 支持的图片文件类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    /**
     * 支持的文档文件类型
     */
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * 最大文件大小 10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public FileUpload uploadFile(MultipartFile file, String category, Long relatedId, Long userId) {
        try {
            // 验证文件
            validateFile(file, category);

            // 获取对应的存储桶
            String bucketName = getBucketName(category);
            
            // 确保存储桶存在
            ensureBucketExists(bucketName);

            // 生成文件路径和名称
            String originalName = file.getOriginalFilename();
            String extension = FileUtil.extName(originalName);
            String fileName = IdUtil.simpleUUID() + "." + extension;
            // 头像不需要子目录，直接放根目录
            String filePath = "avatar".equals(category) ? fileName : buildFilePath(category, fileName);

            // 上传文件到MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 生成访问URL
            String fileUrl = getFileUrl(bucketName, filePath);

            // 保存文件记录到数据库
            FileUpload fileUpload = new FileUpload();
            fileUpload.setUserId(userId);
            fileUpload.setOriginalName(originalName);
            fileUpload.setFileName(fileName);
            fileUpload.setFilePath(filePath);
            fileUpload.setFileUrl(fileUrl);
            fileUpload.setFileSize(file.getSize());
            fileUpload.setFileType(file.getContentType());
            fileUpload.setCategory(category);
            fileUpload.setRelatedId(relatedId);

            this.save(fileUpload);

            log.info("文件上传成功: userId={}, fileName={}, filePath={}", userId, fileName, filePath);
            return fileUpload;

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件上传失败");
        }
    }

    @Override
    public List<FileUpload> uploadFiles(List<MultipartFile> files, String category, Long relatedId, Long userId) {
        List<FileUpload> results = new ArrayList<>();
        for (MultipartFile file : files) {
            FileUpload fileUpload = uploadFile(file, category, relatedId, userId);
            results.add(fileUpload);
        }
        return results;
    }

    @Override
    public boolean deleteFile(Long fileId, Long userId) {
        try {
            FileUpload fileUpload = this.getById(fileId);
            if (fileUpload == null) {
                throw new BusinessException("文件不存在");
            }

            // 验证文件所有者
            if (!fileUpload.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此文件");
            }

            // 从MinIO删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileUpload.getFilePath())
                            .build()
            );

            // 从数据库删除记录
            this.removeById(fileId);

            log.info("文件删除成功: fileId={}, filePath={}", fileId, fileUpload.getFilePath());
            return true;

        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        return getFileUrl(minioConfig.getBucketName(), fileName);
    }
    
    /**
     * 获取文件访问URL（指定桶）
     */
    public String getFileUrl(String bucketName, String fileName) {
        try {
            // 生成预签名URL，有效期7天
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7 * 24 * 60 * 60) // 7天
                            .build()
            );
        } catch (Exception e) {
            log.error("生成文件访问URL失败: {}", e.getMessage(), e);
            return minioConfig.getEndpoint() + "/" + bucketName + "/" + fileName;
        }
    }

    @Override
    public List<FileUpload> getFilesByCategory(String category, Long relatedId) {
        return baseMapper.findByCategoryAndRelatedId(category, relatedId);
    }

    @Override
    public List<FileUpload> getUserFiles(Long userId) {
        return baseMapper.findByUserId(userId);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, String category) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "文件大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "无法识别文件类型");
        }

        // 根据分类验证文件类型
        switch (category) {
            case "avatar":
            case "house":
            case "proof":
                if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                    throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "只支持图片格式文件");
                }
                break;
            case "contract":
                if (!ALLOWED_DOCUMENT_TYPES.contains(contentType) && !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                    throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "只支持图片或PDF文档格式");
                }
                break;
            default:
                // 其他分类允许所有支持的类型
                List<String> allAllowedTypes = new ArrayList<>();
                allAllowedTypes.addAll(ALLOWED_IMAGE_TYPES);
                allAllowedTypes.addAll(ALLOWED_DOCUMENT_TYPES);
                if (!allAllowedTypes.contains(contentType)) {
                    throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "不支持的文件类型");
                }
        }
    }

    /**
     * 根据分类获取对应的存储桶名称
     */
    private String getBucketName(String category) {
        if ("avatar".equals(category)) {
            return minioConfig.getAvatarBucketName();
        }
        return minioConfig.getBucketName();
    }
    
    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists(String bucketName) {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            
            if (!bucketExists) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                // 设置存储桶为公开读取（头像需要公开访问）
                if (bucketName.equals(minioConfig.getAvatarBucketName())) {
                    setPublicReadPolicy(bucketName);
                }
                log.info("创建MinIO存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败: {}", e.getMessage(), e);
            throw new BusinessException("存储服务异常");
        }
    }
    
    /**
     * 设置存储桶为公开读取策略
     */
    private void setPublicReadPolicy(String bucketName) {
        try {
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("设置存储桶公开读取策略: {}", bucketName);
        } catch (Exception e) {
            log.warn("设置存储桶策略失败: {}", e.getMessage());
        }
    }

    /**
     * 构建文件路径
     */
    private String buildFilePath(String category, String fileName) {
        String datePath = DateUtil.format(DateUtil.date(), "yyyy/MM/dd");
        return String.format("%s/%s/%s", category, datePath, fileName);
    }
}
