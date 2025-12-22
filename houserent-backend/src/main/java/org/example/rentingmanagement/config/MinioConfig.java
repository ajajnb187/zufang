package org.example.rentingmanagement.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO对象存储配置类
 * 基于MinIO 8.5.7最新版本
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO服务端点
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 存储桶名称（房源图片等）
     */
    private String bucketName;

    /**
     * 头像存储桶名称
     */
    private String avatarBucketName;

    /**
     * 创建MinIO客户端Bean
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
