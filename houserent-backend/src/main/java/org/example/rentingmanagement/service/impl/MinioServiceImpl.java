package org.example.rentingmanagement.service.impl;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.config.MinioConfig;
import org.example.rentingmanagement.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MinIO文件存储服务实现类
 */
@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 初始化，确保bucket存在
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing MinIO with endpoint: {}, bucket: {}", minioConfig.getEndpoint(), minioConfig.getBucketName());
            
            // 检查存储桶是否存在，不存在则创建
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );

            if (!bucketExists) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );

                // 设置存储桶为公共读取
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": "*",
                                    "Action": "s3:GetObject",
                                    "Resource": "arn:aws:s3:::%s/*"
                                }
                            ]
                        }
                        """.formatted(minioConfig.getBucketName());

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .config(policy)
                                .build()
                );

                log.info("MinIO bucket created and configured: {}", minioConfig.getBucketName());
            } else {
                log.info("MinIO bucket already exists: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", e.getMessage(), e);
            log.warn("MinIO may not be running or credentials may be incorrect. Please check MinIO server status and configuration.");
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            String objectName = folder + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + fileName;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回文件访问URL
            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String folder) {
        List<String> fileUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String fileUrl = uploadFile(file, folder);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                log.error("Batch upload failed for file: {}", file.getOriginalFilename(), e);
                // 继续上传其他文件，不中断整个过程
            }
        }
        
        return fileUrls;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取对象名称
            String objectName = extractObjectNameFromUrl(fileUrl);
            if (objectName == null) {
                return false;
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );

            log.info("File deleted successfully: {}", fileUrl);
            return true;

        } catch (Exception e) {
            log.error("File deletion failed: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public boolean deleteFiles(List<String> fileUrls) {
        boolean allSuccess = true;
        
        for (String fileUrl : fileUrls) {
            if (!deleteFile(fileUrl)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }

    @Override
    public String getFileUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        String bucketPath = "/" + minioConfig.getBucketName() + "/";
        int index = fileUrl.indexOf(bucketPath);
        if (index == -1) {
            return null;
        }
        
        return fileUrl.substring(index + bucketPath.length());
    }
}
