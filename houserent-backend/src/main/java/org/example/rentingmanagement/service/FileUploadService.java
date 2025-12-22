package org.example.rentingmanagement.service;

import org.example.rentingmanagement.entity.FileUpload;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传文件
     */
    FileUpload uploadFile(MultipartFile file, String category, Long relatedId, Long userId);

    /**
     * 批量上传文件
     */
    List<FileUpload> uploadFiles(List<MultipartFile> files, String category, Long relatedId, Long userId);

    /**
     * 删除文件
     */
    boolean deleteFile(Long fileId, Long userId);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String fileName);

    /**
     * 根据分类获取文件列表
     */
    List<FileUpload> getFilesByCategory(String category, Long relatedId);

    /**
     * 获取用户上传的文件
     */
    List<FileUpload> getUserFiles(Long userId);
}
