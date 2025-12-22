package org.example.rentingmanagement.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.FileUpload;
import org.example.rentingmanagement.service.FileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 头像上传专用接口
     * 路径：/api/upload/avatar
     */
    @PostMapping("/api/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileUpload fileUpload = fileUploadService.uploadFile(file, "avatar", userId, userId);
        return Result.success("头像上传成功", fileUpload.getFileUrl());
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
        String fileUrl = fileUploadService.getFileUrl(fileName);
        return Result.success(fileUrl);
    }

    /**
     * 根据分类获取文件列表
     */
    @GetMapping("/api/files/category/{category}")
    public Result<List<FileUpload>> getFilesByCategory(@PathVariable String category,
                                                       @RequestParam(required = false) Long relatedId) {
        List<FileUpload> files = fileUploadService.getFilesByCategory(category, relatedId);
        return Result.success(files);
    }

    /**
     * 获取用户上传的文件
     */
    @GetMapping("/api/files/user")
    public Result<List<FileUpload>> getUserFiles() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<FileUpload> files = fileUploadService.getUserFiles(userId);
        return Result.success(files);
    }
}
