package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.RulesDocument;
import org.example.rentingmanagement.mapper.RulesDocumentMapper;
import org.example.rentingmanagement.service.MinioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 租赁条例文档管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/rules-documents")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class RulesController {

    private final RulesDocumentMapper rulesDocumentMapper;
    private final MinioService minioService;

    /**
     * 获取条例文档列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getRulesList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        log.info("获取条例文档列表: keyword={}, status={}", keyword, status);

        LambdaQueryWrapper<RulesDocument> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(RulesDocument::getTitle, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RulesDocument::getStatus, status);
        }
        wrapper.orderByDesc(RulesDocument::getCreatedAt);

        List<RulesDocument> documents = rulesDocumentMapper.selectList(wrapper);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<Map<String, Object>> result = new ArrayList<>();
        for (RulesDocument doc : documents) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", doc.getDocumentId());
            item.put("title", doc.getTitle());
            item.put("fileName", doc.getFileName());
            item.put("filePath", doc.getFilePath());
            item.put("fileSize", formatFileSize(doc.getFileSize()));
            item.put("fileType", doc.getFileType());
            item.put("description", doc.getDescription());
            item.put("status", doc.getStatus());
            item.put("version", doc.getVersion());
            item.put("downloadCount", doc.getDownloadCount());
            item.put("uploadTime", doc.getCreatedAt() != null ? doc.getCreatedAt().format(formatter) : null);
            item.put("effectiveDate", doc.getEffectiveDate() != null ? doc.getEffectiveDate().format(formatter) : null);
            result.add(item);
        }

        return Result.success(result);
    }

    /**
     * 上传条例文档
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String version) {

        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        String fileType = getFileType(originalFilename);

        if (!isValidFileType(fileType)) {
            return Result.error("只支持PDF、DOC、DOCX格式的文档");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过10MB");
        }

        try {
            Long uploaderId = StpUtil.getLoginIdAsLong();

            // 上传到MinIO
            String filePath = minioService.uploadFile(file, "rules");

            // 保存文档记录
            RulesDocument document = new RulesDocument();
            document.setTitle(title != null && !title.isEmpty() ? title : getFileNameWithoutExtension(originalFilename));
            document.setFileName(originalFilename);
            document.setFilePath(filePath);
            document.setFileSize(file.getSize());
            document.setFileType(fileType);
            document.setDescription(description);
            document.setVersion(version != null ? version : "1.0");
            document.setStatus("pending");
            document.setUploaderId(uploaderId);
            document.setDownloadCount(0);
            document.setCreatedAt(LocalDateTime.now());

            rulesDocumentMapper.insert(document);

            log.info("条例文档上传成功: documentId={}, title={}", document.getDocumentId(), document.getTitle());

            Map<String, Object> result = new HashMap<>();
            result.put("id", document.getDocumentId());
            result.put("title", document.getTitle());
            result.put("fileName", document.getFileName());
            result.put("filePath", document.getFilePath());

            return Result.success(result);
        } catch (Exception e) {
            log.error("条例文档上传失败: {}", e.getMessage());
            return Result.error("文档上传失败：" + e.getMessage());
        }
    }

    /**
     * 更新条例文档信息
     */
    @PutMapping("/{documentId}")
    public Result<String> updateDocument(@PathVariable Long documentId, @RequestBody Map<String, Object> body) {
        RulesDocument document = rulesDocumentMapper.selectById(documentId);
        if (document == null) {
            return Result.error("文档不存在");
        }

        if (body.containsKey("title")) {
            document.setTitle((String) body.get("title"));
        }
        if (body.containsKey("description")) {
            document.setDescription((String) body.get("description"));
        }
        if (body.containsKey("version")) {
            document.setVersion((String) body.get("version"));
        }
        if (body.containsKey("status")) {
            document.setStatus((String) body.get("status"));
        }
        if (body.containsKey("effectiveDate") && body.get("effectiveDate") != null) {
            document.setEffectiveDate(LocalDateTime.parse((String) body.get("effectiveDate"), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        document.setUpdatedAt(LocalDateTime.now());

        rulesDocumentMapper.updateById(document);
        log.info("条例文档更新成功: documentId={}", documentId);
        return Result.success("文档更新成功");
    }

    /**
     * 删除条例文档
     */
    @DeleteMapping("/{documentId}")
    public Result<String> deleteDocument(@PathVariable Long documentId) {
        RulesDocument document = rulesDocumentMapper.selectById(documentId);
        if (document == null) {
            return Result.error("文档不存在");
        }

        // 从MinIO删除文件
        try {
            if (document.getFilePath() != null) {
                minioService.deleteFile(document.getFilePath());
            }
        } catch (Exception e) {
            log.warn("删除MinIO文件失败: {}", e.getMessage());
        }

        rulesDocumentMapper.deleteById(documentId);
        log.info("条例文档删除成功: documentId={}", documentId);
        return Result.success("文档删除成功");
    }

    /**
     * 设置文档状态（生效/待生效）
     */
    @PostMapping("/{documentId}/activate")
    public Result<String> activateDocument(@PathVariable Long documentId) {
        RulesDocument document = rulesDocumentMapper.selectById(documentId);
        if (document == null) {
            return Result.error("文档不存在");
        }

        document.setStatus("active");
        document.setEffectiveDate(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        rulesDocumentMapper.updateById(document);

        log.info("条例文档已生效: documentId={}", documentId);
        return Result.success("文档已设置为生效状态");
    }

    /**
     * 撤销文档生效状态
     */
    @PostMapping("/{documentId}/deactivate")
    public Result<String> deactivateDocument(@PathVariable Long documentId) {
        RulesDocument document = rulesDocumentMapper.selectById(documentId);
        if (document == null) {
            return Result.error("文档不存在");
        }

        document.setStatus("pending");
        document.setUpdatedAt(LocalDateTime.now());
        rulesDocumentMapper.updateById(document);

        log.info("条例文档已撤销生效: documentId={}", documentId);
        return Result.success("文档已撤销生效状态");
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{documentId}")
    public Result<Map<String, Object>> getDocumentDetail(@PathVariable Long documentId) {
        RulesDocument document = rulesDocumentMapper.selectById(documentId);
        if (document == null) {
            return Result.error("文档不存在");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", document.getDocumentId());
        detail.put("title", document.getTitle());
        detail.put("fileName", document.getFileName());
        detail.put("filePath", document.getFilePath());
        detail.put("fileSize", formatFileSize(document.getFileSize()));
        detail.put("fileType", document.getFileType());
        detail.put("description", document.getDescription());
        detail.put("status", document.getStatus());
        detail.put("version", document.getVersion());
        detail.put("downloadCount", document.getDownloadCount());
        detail.put("uploadTime", document.getCreatedAt() != null ? document.getCreatedAt().format(formatter) : null);
        detail.put("effectiveDate", document.getEffectiveDate() != null ? document.getEffectiveDate().format(formatter) : null);

        return Result.success(detail);
    }

    /**
     * 预览/下载文档
     */
    @GetMapping("/{documentId}/download")
    public Result<String> downloadDocument(@PathVariable Long documentId) {
        RulesDocument document = rulesDocumentMapper.selectById(documentId);
        if (document == null) {
            return Result.error("文档不存在");
        }

        // 增加下载次数
        rulesDocumentMapper.incrementDownloadCount(documentId);

        return Result.success(document.getFilePath());
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileType(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 获取不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }

    /**
     * 验证文件类型
     */
    private boolean isValidFileType(String fileType) {
        return "pdf".equals(fileType) || "doc".equals(fileType) || "docx".equals(fileType);
    }
}
