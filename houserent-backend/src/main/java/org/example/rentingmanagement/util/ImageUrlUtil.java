package org.example.rentingmanagement.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.config.ServerConfig;
import org.example.rentingmanagement.service.MinioService;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 图片URL工具类
 * 用于处理图片路径与URL的转换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUrlUtil {

    private final ServerConfig serverConfig;
    private final MinioService minioService;

    /**
     * 将相对路径转换为代理URL
     * 例如: avatars/xxx.jpg -> http://192.168.31.106:8888/api/img/avatars/xxx.jpg
     * 
     * @param relativePath 相对路径 (bucket/objectName)
     * @return 代理URL
     */
    public String toProxyUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            log.warn("toProxyUrl: 输入路径为空");
            return null;
        }
        
        log.debug("toProxyUrl 输入: {}", relativePath);
        
        // 如果已经是完整URL，直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            // 兼容旧数据：如果是完整MinIO URL，尝试提取相对路径再转换
            String result = convertOldUrlToProxy(relativePath);
            log.debug("toProxyUrl 输出(旧URL转换): {}", result);
            return result;
        }
        
        // 构建代理URL
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        String serverUrl = serverConfig.getServerUrl();
        String result = serverUrl + "/api/img/" + cleanPath;
        log.info("toProxyUrl - ServerUrl: {}, 输入: {}, 输出: {}", serverUrl, relativePath, result);
        return result;
    }

    /**
     * 从URL提取相对路径
     * 例如: http://192.168.31.106:9000/avatars/xxx.jpg -> avatars/xxx.jpg
     * 例如: http://192.168.31.106:8888/api/img/avatars/xxx.jpg -> avatars/xxx.jpg
     * 
     * @param url 完整URL或相对路径
     * @return 相对路径
     */
    public String extractRelativePath(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // 如果不是URL格式，检查是否已经是相对路径
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            // 如果以 /api/img/ 开头，去掉这个前缀
            if (url.startsWith("/api/img/")) {
                return url.substring(9); // 去掉 "/api/img/"
            }
            if (url.startsWith("api/img/")) {
                return url.substring(8); // 去掉 "api/img/"
            }
            return url;
        }
        
        try {
            // 查找第三个斜杠后的内容（即bucket/objectName部分）
            int firstSlash = url.indexOf("//");
            if (firstSlash == -1) {
                return url;
            }
            
            int thirdSlash = url.indexOf("/", firstSlash + 2);
            if (thirdSlash == -1) {
                return url;
            }
            
            // 提取path部分
            String pathPart = url.substring(thirdSlash + 1);
            
            // 移除查询参数
            int queryIndex = pathPart.indexOf("?");
            if (queryIndex != -1) {
                pathPart = pathPart.substring(0, queryIndex);
            }
            
            // 如果path包含 api/img/，去掉这个前缀（代理URL格式）
            if (pathPart.startsWith("api/img/")) {
                pathPart = pathPart.substring(8);
            }
            
            return pathPart;
        } catch (Exception e) {
            log.warn("Failed to extract relative path from URL: {}", url, e);
            return url;
        }
    }

    /**
     * 转换旧的MinIO URL为代理URL
     * 兼容已存在的完整URL数据
     */
    private String convertOldUrlToProxy(String oldUrl) {
        String relativePath = extractRelativePath(oldUrl);
        if (relativePath == null || relativePath.equals(oldUrl)) {
            return oldUrl;
        }
        return serverConfig.getServerUrl() + "/api/img/" + relativePath;
    }

    /**
     * 构建相对路径
     * 
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return 相对路径
     */
    public String buildRelativePath(String bucketName, String objectName) {
        if (bucketName == null || objectName == null) {
            return null;
        }
        return bucketName + "/" + objectName;
    }

    /**
     * 将相对路径转换为Base64 Data URL
     * 用于微信小程序真机环境，避免域名限制
     * 
     * @param relativePath 相对路径 (bucket/objectName)
     * @return Base64 Data URL (data:image/jpeg;base64,...)
     */
    public String toBase64DataUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            log.warn("toBase64DataUrl: 输入路径为空");
            return null;
        }

        try {
            // 如果已经是Base64格式，直接返回
            if (relativePath.startsWith("data:image/")) {
                return relativePath;
            }

            // 如果是完整URL，提取相对路径
            if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
                relativePath = extractRelativePath(relativePath);
            }

            // 分离bucket和objectName
            String[] parts = relativePath.split("/", 2);
            if (parts.length != 2) {
                log.warn("toBase64DataUrl: 路径格式错误: {}", relativePath);
                return null;
            }

            String bucketName = parts[0];
            String objectName = parts[1];

            // 从MinIO获取图片字节
            byte[] imageBytes = minioService.getFileBytes(bucketName, objectName);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("toBase64DataUrl: 无法获取图片数据: {}, 返回占位符", relativePath);
                // 返回一个1x1透明PNG占位符
                return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
            }

            // 根据文件扩展名确定MIME类型
            String mimeType = getMimeType(objectName);

            // 转换为Base64
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            // 构建Data URL
            String dataUrl = "data:" + mimeType + ";base64," + base64;

            log.info("toBase64DataUrl - 输入: {}, 输出大小: {}KB", relativePath, imageBytes.length / 1024);

            return dataUrl;
        } catch (Exception e) {
            log.error("toBase64DataUrl 失败: {}, 返回占位符", relativePath, e);
            // 返回一个1x1透明PNG占位符
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        }
    }

    /**
     * 根据文件名获取MIME类型
     */
    private String getMimeType(String fileName) {
        if (fileName == null) {
            return "image/jpeg";
        }

        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "image/jpeg"; // 默认
        }
    }
}
