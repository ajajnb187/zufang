package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件上传数据访问层
 */
@Mapper
public interface FileUploadMapper extends BaseMapper<FileUpload> {

    /**
     * 根据用户ID查询文件列表
     */
    @Select("SELECT * FROM file_uploads WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<FileUpload> findByUserId(Long userId);

    /**
     * 根据分类和关联ID查询文件
     */
    @Select("SELECT * FROM file_uploads WHERE category = #{category} AND related_id = #{relatedId} ORDER BY created_at DESC")
    List<FileUpload> findByCategoryAndRelatedId(String category, Long relatedId);

    /**
     * 根据文件路径查询文件
     */
    @Select("SELECT * FROM file_uploads WHERE file_path = #{filePath}")
    FileUpload findByFilePath(String filePath);

    /**
     * 统计用户上传的文件数量
     */
    @Select("SELECT COUNT(*) FROM file_uploads WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
