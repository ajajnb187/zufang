package org.example.rentingmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.rentingmanagement.dto.ForumPostCreateRequest;
import org.example.rentingmanagement.dto.ForumReplyCreateRequest;
import org.example.rentingmanagement.entity.ForumPost;
import org.example.rentingmanagement.entity.ForumReply;

import java.util.List;

/**
 * 交流论坛服务接口
 */
public interface ForumService {

    /**
     * 创建论坛帖子
     */
    ForumPost createPost(ForumPostCreateRequest request, Long userId);

    /**
     * 获取小区论坛帖子列表
     */
    IPage<ForumPost> getForumPosts(Long communityId, String postType, Integer pageNum, Integer pageSize);

    /**
     * 获取帖子详情
     */
    ForumPost getPostDetail(Long postId);

    /**
     * 更新帖子
     */
    ForumPost updatePost(Long postId, ForumPostCreateRequest request, Long userId);

    /**
     * 删除帖子
     */
    boolean deletePost(Long postId, Long userId);

    /**
     * 关闭帖子
     */
    boolean closePost(Long postId, Long userId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long postId);

    /**
     * 创建回复
     */
    ForumReply createReply(Long postId, ForumReplyCreateRequest request, Long userId);

    /**
     * 获取帖子的回复列表
     */
    List<ForumReply> getPostReplies(Long postId);

    /**
     * 删除回复
     */
    boolean deleteReply(Long replyId, Long userId);

    /**
     * 获取用户发布的帖子
     */
    IPage<ForumPost> getUserPosts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 管理员删除帖子
     */
    boolean adminDeletePost(Long postId, Long adminId, String reason);

    /**
     * 管理员删除回复
     */
    boolean adminDeleteReply(Long replyId, Long adminId, String reason);

    /**
     * 检查用户是否可以在小区发帖
     */
    boolean canPostInCommunity(Long userId, Long communityId);
}
