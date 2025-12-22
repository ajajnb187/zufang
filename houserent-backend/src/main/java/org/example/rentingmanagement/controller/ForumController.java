package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.dto.ForumPostCreateRequest;
import org.example.rentingmanagement.dto.ForumReplyCreateRequest;
import org.example.rentingmanagement.entity.ForumPost;
import org.example.rentingmanagement.entity.ForumReply;
import org.example.rentingmanagement.service.ForumService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交流论坛控制器
 */
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    /**
     * 发布论坛帖子
     */
    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody @Valid ForumPostCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ForumPost post = forumService.createPost(request, userId);
        return Result.success("帖子发布成功", post);
    }

    /**
     * 获取小区论坛帖子列表
     */
    @GetMapping("/posts")
    public Result<IPage<ForumPost>> getForumPosts(@RequestParam Long communityId,
                                                 @RequestParam(required = false) String postType,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<ForumPost> posts = forumService.getForumPosts(communityId, postType, pageNum, pageSize);
        return Result.success(posts);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{postId}")
    public Result<ForumPost> getPostDetail(@PathVariable Long postId) {
        // 增加浏览次数
        forumService.incrementViewCount(postId);
        
        ForumPost post = forumService.getPostDetail(postId);
        return Result.success(post);
    }

    /**
     * 更新帖子
     */
    @PutMapping("/posts/{postId}")
    public Result<ForumPost> updatePost(@PathVariable Long postId,
                                       @RequestBody @Valid ForumPostCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ForumPost post = forumService.updatePost(postId, request, userId);
        return Result.success("帖子更新成功", post);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();
        forumService.deletePost(postId, userId);
        return Result.success("帖子删除成功");
    }

    /**
     * 关闭帖子（楼主操作）
     */
    @PostMapping("/posts/{postId}/close")
    public Result<Void> closePost(@PathVariable Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();
        forumService.closePost(postId, userId);
        return Result.success("帖子已关闭");
    }

    /**
     * 回复帖子
     */
    @PostMapping("/posts/{postId}/replies")
    public Result<ForumReply> createReply(@PathVariable Long postId,
                                         @RequestBody @Valid ForumReplyCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ForumReply reply = forumService.createReply(postId, request, userId);
        return Result.success("回复成功", reply);
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/posts/{postId}/replies")
    public Result<List<ForumReply>> getPostReplies(@PathVariable Long postId) {
        List<ForumReply> replies = forumService.getPostReplies(postId);
        return Result.success(replies);
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long replyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        forumService.deleteReply(replyId, userId);
        return Result.success("回复删除成功");
    }

    /**
     * 获取用户发布的帖子
     */
    @GetMapping("/posts/user")
    public Result<IPage<ForumPost>> getUserPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<ForumPost> posts = forumService.getUserPosts(userId, pageNum, pageSize);
        return Result.success(posts);
    }

    /**
     * 管理员删除帖子
     */
    @DeleteMapping("/admin/posts/{postId}")
    @SaCheckRole("admin")
    public Result<Void> adminDeletePost(@PathVariable Long postId,
                                       @RequestParam(required = false) String reason) {
        Long adminId = StpUtil.getLoginIdAsLong();
        forumService.adminDeletePost(postId, adminId, reason);
        return Result.success("帖子已删除");
    }

    /**
     * 管理员删除回复
     */
    @DeleteMapping("/admin/replies/{replyId}")
    @SaCheckRole("admin")
    public Result<Void> adminDeleteReply(@PathVariable Long replyId,
                                        @RequestParam(required = false) String reason) {
        Long adminId = StpUtil.getLoginIdAsLong();
        forumService.adminDeleteReply(replyId, adminId, reason);
        return Result.success("回复已删除");
    }
}
