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
import java.io.ByteArrayOutputStream;
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

            // 返回相对路径（bucket/objectName），不再返回完整URL
            String relativePath = minioConfig.getBucketName() + "/" + objectName;
            log.info("File uploaded successfully, relative path: {}", relativePath);
            return relativePath;

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
        // 返回相对路径（bucket/objectName）
        return minioConfig.getBucketName() + "/" + objectName;
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
     * 从URL或相对路径中提取对象名称
     */
    private String extractObjectNameFromUrl(String filePathOrUrl) {
        if (filePathOrUrl == null || filePathOrUrl.isEmpty()) {
            return null;
        }
        
        // 如果是相对路径格式（bucket/objectName），直接提取objectName
        String bucketPrefix = minioConfig.getBucketName() + "/";
        if (filePathOrUrl.startsWith(bucketPrefix)) {
            return filePathOrUrl.substring(bucketPrefix.length());
        }
        
        // 兼容旧的完整URL格式
        String bucketPath = "/" + minioConfig.getBucketName() + "/";
        int index = filePathOrUrl.indexOf(bucketPath);
        if (index == -1) {
            return null;
        }
        
        String pathPart = filePathOrUrl.substring(index + bucketPath.length());
        // 移除查询参数
        int queryIndex = pathPart.indexOf("?");
        if (queryIndex != -1) {
            pathPart = pathPart.substring(0, queryIndex);
        }
        
        return pathPart;
    }

    /**
     * 获取文件字节数组
     * 用于Base64编码
     */
    @Override
    public byte[] getFileBytes(String bucketName, String objectName) {
        try {
            log.debug("获取文件字节: bucket={}, object={}", bucketName, objectName);
            
            // 从MinIO获取文件流
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            
            // 读取所有字节
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
            stream.close();
            
            byte[] result = buffer.toByteArray();
            log.info("成功获取文件字节: bucket={}, object={}, size={}KB", 
                    bucketName, objectName, result.length / 1024);
            
            return result;
        } catch (Exception e) {
            log.error("获取文件字节失败: bucket={}, object={}", bucketName, objectName, e);
            return null;
        }
    }
}
