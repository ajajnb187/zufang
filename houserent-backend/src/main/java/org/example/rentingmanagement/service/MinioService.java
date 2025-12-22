package org.example.rentingmanagement.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * MinIO文件存储服务接口
 */
public interface MinioService {

    /**
     * 上传单个文件
     * @param file 文件
     * @param folder 文件夹路径
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 批量上传文件
     * @param files 文件列表
     * @param folder 文件夹路径
     * @return 文件访问URL列表
     */
    List<String> uploadFiles(List<MultipartFile> files, String folder);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @return 是否删除成功
     */
    boolean deleteFiles(List<String> fileUrls);

    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    String getFileUrl(String objectName);

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String objectName);
}
