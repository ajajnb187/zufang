package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.FileUpload;
import org.example.rentingmanagement.service.FileUploadService;
import org.example.rentingmanagement.util.ImageUrlUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final MinioClient minioClient;
    private final ImageUrlUtil imageUrlUtil;

    /**
     * 头像上传专用接口
     * 路径：/api/upload/avatar
     */
    @PostMapping("/api/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileUpload fileUpload = fileUploadService.uploadFile(file, "avatar", userId, userId);
        // 将相对路径转换为Base64
        String base64Url = imageUrlUtil.toBase64DataUrl(fileUpload.getFileUrl());
        return Result.success("头像上传成功", base64Url);
    }

    /**
     * 单文件上传
     */
    @PostMapping("/api/files/upload")
    public Result<FileUpload> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam String category,
                                         @RequestParam(required = false) Long relatedId) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileUpload fileUpload = fileUploadService.uploadFile(file, category, relatedId, userId);
        // 转换为Base64
        fileUpload.setFileUrl(imageUrlUtil.toBase64DataUrl(fileUpload.getFileUrl()));
        return Result.success("文件上传成功", fileUpload);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/api/files/upload-batch")
    public Result<List<FileUpload>> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                                @RequestParam String category,
                                                @RequestParam(required = false) Long relatedId) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<FileUpload> fileUploads = fileUploadService.uploadFiles(files, category, relatedId, userId);
        // 转换为Base64
        fileUploads.forEach(f -> f.setFileUrl(imageUrlUtil.toBase64DataUrl(f.getFileUrl())));
        return Result.success("文件批量上传成功", fileUploads);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/api/files/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();
        fileUploadService.deleteFile(fileId, userId);
        return Result.success("文件删除成功");
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/api/files/{fileName}/url")
    public Result<String> getFileUrl(@PathVariable String fileName) {
        String relativePath = fileUploadService.getFileUrl(fileName);
        String base64Url = imageUrlUtil.toBase64DataUrl(relativePath);
        return Result.success(base64Url);
    }

    /**
     * 根据分类获取文件列表
     */
    @GetMapping("/api/files/category/{category}")
    public Result<List<FileUpload>> getFilesByCategory(@PathVariable String category,
                                                       @RequestParam(required = false) Long relatedId) {
        List<FileUpload> files = fileUploadService.getFilesByCategory(category, relatedId);
        // 转换为Base64
        files.forEach(f -> f.setFileUrl(imageUrlUtil.toBase64DataUrl(f.getFileUrl())));
        return Result.success(files);
    }

    /**
     * 获取用户上传的文件
     */
    @GetMapping("/api/files/user")
    public Result<List<FileUpload>> getUserFiles() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<FileUpload> files = fileUploadService.getUserFiles(userId);
        // 转换为Base64
        files.forEach(f -> f.setFileUrl(imageUrlUtil.toBase64DataUrl(f.getFileUrl())));
        return Result.success(files);
    }
    
    /**
     * 图片代理接口 - 解决真机无法直接访问MinIO的问题
     * 通过API服务器代理MinIO图片请求
     */
    @GetMapping("/api/img/{bucket}/**")
    public void proxyImage(@PathVariable String bucket,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        String uri = request.getRequestURI();
        // 提取对象路径: /api/img/houserent/xxx -> xxx
        String prefix = "/api/img/" + bucket + "/";
        int startIndex = uri.indexOf(prefix);
        if (startIndex == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String objectName = uri.substring(startIndex + prefix.length());
        
        if (objectName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            log.info("=== 代理图片请求 ===");
            log.info("完整URI: {}", uri);
            log.info("Bucket: {}", bucket);
            log.info("ObjectName: {}", objectName);
            log.info("==================");
            
            // 从MinIO获取文件
            InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
            
            // 设置响应头
            String contentType = getContentType(objectName);
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "max-age=86400");
            response.setHeader("Access-Control-Allow-Origin", "*");
            
            log.info("返回图片: ContentType={}", contentType);
            
            // 写入响应
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();
            inputStream.close();
            
            log.info("图片代理成功: 大小={}KB", totalBytes / 1024);
            
        } catch (Exception e) {
            log.error("代理图片失败: bucket={}, object={}, error={}", bucket, objectName, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private String getContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
