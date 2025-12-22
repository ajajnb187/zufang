package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.ForumReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 论坛回复数据访问层
 */
@Mapper
public interface ForumReplyMapper extends BaseMapper<ForumReply> {

    /**
     * 查询帖子的回复列表
     */
    @Select("SELECT * FROM forum_replies WHERE post_id = #{postId} AND is_deleted = 0 ORDER BY created_at ASC")
    List<ForumReply> findByPostId(Long postId);

    /**
     * 查询回复的子回复
     */
    @Select("SELECT * FROM forum_replies WHERE parent_reply_id = #{parentId} AND is_deleted = 0 ORDER BY created_at ASC")
    List<ForumReply> findByParentId(Long parentId);

    /**
     * 统计帖子回复数
     */
    @Select("SELECT COUNT(*) FROM forum_replies WHERE post_id = #{postId} AND is_deleted = 0")
    int countByPostId(Long postId);
}
