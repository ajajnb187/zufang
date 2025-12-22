package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件上传实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("file_uploads")
public class FileUpload {

    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    @TableField("user_id")
    private Long userId;

    @TableField("original_name")
    private String originalName;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_type")
    private String fileType;

    @TableField("category")
    private String category; // avatar, house, proof, contract等

    @TableField("related_id")
    private Long relatedId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
