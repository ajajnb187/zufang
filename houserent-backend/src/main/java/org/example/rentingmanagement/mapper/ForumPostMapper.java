package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.rentingmanagement.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 论坛帖子数据访问层
 */
@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 分页查询小区论坛帖子
     */
    @Select("SELECT * FROM forum_posts WHERE community_id = #{communityId} AND status = 'active' " +
            "AND (#{postType} IS NULL OR post_type = #{postType}) " +
            "ORDER BY " +
            "CASE WHEN is_urgent = 1 THEN 0 ELSE 1 END, " +
            "created_at DESC")
    IPage<ForumPost> findByCommunityWithType(Page<ForumPost> page, 
                                              @Param("communityId") Long communityId,
                                              @Param("postType") String postType);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE forum_posts SET view_count = view_count + 1 WHERE post_id = #{postId}")
    void incrementViewCount(Long postId);

    /**
     * 更新回复数量
     */
    @Update("UPDATE forum_posts SET reply_count = #{count} WHERE post_id = #{postId}")
    void updateReplyCount(@Param("postId") Long postId, @Param("count") Integer count);

    /**
     * 查询用户发布的帖子
     */
    @Select("SELECT * FROM forum_posts WHERE user_id = #{userId} AND status != 'deleted' ORDER BY created_at DESC")
    IPage<ForumPost> findByUserId(Page<ForumPost> page, Long userId);
}
