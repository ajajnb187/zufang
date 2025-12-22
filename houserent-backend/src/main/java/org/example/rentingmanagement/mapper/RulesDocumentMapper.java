package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.rentingmanagement.entity.RulesDocument;

import java.util.List;

/**
 * 租赁条例文档Mapper接口
 */
@Mapper
public interface RulesDocumentMapper extends BaseMapper<RulesDocument> {

    @Select("SELECT * FROM rules_documents WHERE status = 'active' ORDER BY created_at DESC")
    List<RulesDocument> findActiveDocuments();

    @Update("UPDATE rules_documents SET download_count = download_count + 1 WHERE document_id = #{documentId}")
    int incrementDownloadCount(@Param("documentId") Long documentId);

    @Select("SELECT * FROM rules_documents WHERE title LIKE CONCAT('%', #{keyword}, '%') ORDER BY created_at DESC")
    List<RulesDocument> searchByTitle(@Param("keyword") String keyword);
}