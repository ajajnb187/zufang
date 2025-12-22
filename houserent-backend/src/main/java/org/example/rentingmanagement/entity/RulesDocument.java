package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租赁条例文档实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rules_documents")
public class RulesDocument {

    @TableId(value = "document_id", type = IdType.AUTO)
    private Long documentId;

    @TableField("title")
    private String title;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_type")
    private String fileType;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("uploader_id")
    private Long uploaderId;

    @TableField("version")
    private String version;

    @TableField("effective_date")
    private LocalDateTime effectiveDate;

    @TableField("download_count")
    private Integer downloadCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}