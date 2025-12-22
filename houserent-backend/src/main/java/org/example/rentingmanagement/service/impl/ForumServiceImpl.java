package org.example.rentingmanagement.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.dto.ForumPostCreateRequest;
import org.example.rentingmanagement.dto.ForumReplyCreateRequest;
import org.example.rentingmanagement.entity.ForumPost;
import org.example.rentingmanagement.entity.ForumReply;
import org.example.rentingmanagement.mapper.CommunityVerificationMapper;
import org.example.rentingmanagement.mapper.ForumPostMapper;
import org.example.rentingmanagement.mapper.ForumReplyMapper;
import org.example.rentingmanagement.service.ForumService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 交流论坛服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumService {

    private final ForumReplyMapper forumReplyMapper;
    private final CommunityVerificationMapper communityVerificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost createPost(ForumPostCreateRequest request, Long userId) {
        try {
            // 检查用户是否有在该小区发帖的权限
            if (!canPostInCommunity(userId, request.getCommunityId())) {
                throw new BusinessException(ResultCode.COMMUNITY_NOT_VERIFIED, "请先完成小区身份认证");
            }

            // 创建帖子实体
            ForumPost post = new ForumPost();
            post.setUserId(userId);
            post.setCommunityId(request.getCommunityId());
            post.setPostType(request.getPostType());
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setBudgetMin(request.getBudgetMin());
            post.setBudgetMax(request.getBudgetMax());
            post.setExpectedArea(request.getExpectedArea());
            post.setExpectedType(request.getExpectedType());
            post.setImages(request.getImages() != null ? JSON.toJSONString(request.getImages()) : null);
            post.setIsUrgent(request.getIsUrgent());
            post.setStatus("active");
            post.setViewCount(0);
            post.setReplyCount(0);

            this.save(post);

            log.info("论坛帖子创建成功: postId={}, userId={}", post.getPostId(), userId);
            return post;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建论坛帖子失败: {}", e.getMessage(), e);
            throw new BusinessException("发布帖子失败");
        }
    }

    @Override
    public IPage<ForumPost> getForumPosts(Long communityId, String postType, Integer pageNum, Integer pageSize) {
        try {
            Page<ForumPost> page = new Page<>(pageNum, pageSize);
            return baseMapper.findByCommunityWithType(page, communityId, postType);
        } catch (Exception e) {
            log.error("获取论坛帖子列表失败: communityId={}, postType={}", communityId, postType, e);
            throw new BusinessException("获取帖子列表失败");
        }
    }

    @Override
    public ForumPost getPostDetail(Long postId) {
        try {
            ForumPost post = this.getById(postId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }

            if ("deleted".equals(post.getStatus())) {
                throw new BusinessException("帖子已被删除");
            }

            return post;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取帖子详情失败: postId={}", postId, e);
            throw new BusinessException("获取帖子详情失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost updatePost(Long postId, ForumPostCreateRequest request, Long userId) {
        try {
            ForumPost post = this.getById(postId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }

            // 验证帖子所有者
            if (!post.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此帖子");
            }

            // 检查帖子状态
            if ("closed".equals(post.getStatus()) || "deleted".equals(post.getStatus())) {
                throw new BusinessException("帖子状态错误，无法修改");
            }

            // 更新帖子信息
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setBudgetMin(request.getBudgetMin());
            post.setBudgetMax(request.getBudgetMax());
            post.setExpectedArea(request.getExpectedArea());
            post.setExpectedType(request.getExpectedType());
            post.setImages(request.getImages() != null ? JSON.toJSONString(request.getImages()) : null);
            post.setIsUrgent(request.getIsUrgent());

            this.updateById(post);

            log.info("论坛帖子更新成功: postId={}, userId={}", postId, userId);
            return post;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新论坛帖子失败: {}", e.getMessage(), e);
            throw new BusinessException("更新帖子失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long postId, Long userId) {
        try {
            ForumPost post = this.getById(postId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }

            // 验证帖子所有者
            if (!post.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此帖子");
            }

            // 软删除帖子
            post.setStatus("deleted");
            this.updateById(post);

            log.info("论坛帖子删除成功: postId={}, userId={}", postId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除论坛帖子失败: {}", e.getMessage(), e);
            throw new BusinessException("删除帖子失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePost(Long postId, Long userId) {
        try {
            ForumPost post = this.getById(postId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }

            // 验证帖子所有者
            if (!post.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权关闭此帖子");
            }

            // 关闭帖子
            post.setStatus("closed");
            this.updateById(post);

            log.info("论坛帖子关闭成功: postId={}, userId={}", postId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("关闭论坛帖子失败: {}", e.getMessage(), e);
            throw new BusinessException("关闭帖子失败");
        }
    }

    @Override
    public void incrementViewCount(Long postId) {
        try {
            baseMapper.incrementViewCount(postId);
        } catch (Exception e) {
            log.error("增加帖子浏览次数失败: postId={}", postId, e);
            // 浏览次数更新失败不抛出异常，不影响正常流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumReply createReply(Long postId, ForumReplyCreateRequest request, Long userId) {
        try {
            // 检查帖子是否存在
            ForumPost post = this.getById(postId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }

            // 检查帖子状态
            if (!"active".equals(post.getStatus())) {
                throw new BusinessException("帖子已关闭或删除，无法回复");
            }

            // 检查用户权限（需要小区认证）
            if (!canPostInCommunity(userId, post.getCommunityId())) {
                throw new BusinessException(ResultCode.COMMUNITY_NOT_VERIFIED, "请先完成小区身份认证");
            }

            // 创建回复实体
            ForumReply reply = new ForumReply();
            reply.setPostId(postId);
            reply.setUserId(userId);
            reply.setParentReplyId(request.getParentReplyId());
            reply.setContent(request.getContent());
            reply.setImages(request.getImages() != null ? JSON.toJSONString(request.getImages()) : null);
            reply.setIsDeleted(false);

            forumReplyMapper.insert(reply);

            // 更新帖子回复数量
            int replyCount = forumReplyMapper.countByPostId(postId);
            baseMapper.updateReplyCount(postId, replyCount);

            log.info("论坛回复创建成功: replyId={}, postId={}, userId={}", reply.getReplyId(), postId, userId);
            return reply;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建论坛回复失败: {}", e.getMessage(), e);
            throw new BusinessException("回复帖子失败");
        }
    }

    @Override
    public List<ForumReply> getPostReplies(Long postId) {
        try {
            return forumReplyMapper.findByPostId(postId);
        } catch (Exception e) {
            log.error("获取帖子回复列表失败: postId={}", postId, e);
            throw new BusinessException("获取回复列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReply(Long replyId, Long userId) {
        try {
            ForumReply reply = forumReplyMapper.selectById(replyId);
            if (reply == null) {
                throw new BusinessException("回复不存在");
            }

            // 验证回复所有者
            if (!reply.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此回复");
            }

            // 软删除回复
            reply.setIsDeleted(true);
            forumReplyMapper.updateById(reply);

            // 更新帖子回复数量
            int replyCount = forumReplyMapper.countByPostId(reply.getPostId());
            baseMapper.updateReplyCount(reply.getPostId(), replyCount);

            log.info("论坛回复删除成功: replyId={}, userId={}", replyId, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除论坛回复失败: {}", e.getMessage(), e);
            throw new BusinessException("删除回复失败");
        }
    }

    @Override
    public IPage<ForumPost> getUserPosts(Long userId, Integer pageNum, Integer pageSize) {
        try {
            Page<ForumPost> page = new Page<>(pageNum, pageSize);
            return baseMapper.findByUserId(page, userId);
        } catch (Exception e) {
            log.error("获取用户帖子列表失败: userId={}", userId, e);
            throw new BusinessException("获取帖子列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeletePost(Long postId, Long adminId, String reason) {
        try {
            ForumPost post = this.getById(postId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }

            // 管理员删除帖子
            post.setStatus("deleted");
            this.updateById(post);

            // TODO: 记录管理员操作日志
            log.info("管理员删除论坛帖子: postId={}, adminId={}, reason={}", postId, adminId, reason);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员删除论坛帖子失败: {}", e.getMessage(), e);
            throw new BusinessException("删除帖子失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeleteReply(Long replyId, Long adminId, String reason) {
        try {
            ForumReply reply = forumReplyMapper.selectById(replyId);
            if (reply == null) {
                throw new BusinessException("回复不存在");
            }

            // 管理员删除回复
            reply.setIsDeleted(true);
            forumReplyMapper.updateById(reply);

            // 更新帖子回复数量
            int replyCount = forumReplyMapper.countByPostId(reply.getPostId());
            baseMapper.updateReplyCount(reply.getPostId(), replyCount);

            // TODO: 记录管理员操作日志
            log.info("管理员删除论坛回复: replyId={}, adminId={}, reason={}", replyId, adminId, reason);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员删除论坛回复失败: {}", e.getMessage(), e);
            throw new BusinessException("删除回复失败");
        }
    }

    @Override
    public boolean canPostInCommunity(Long userId, Long communityId) {
        try {
            return communityVerificationMapper.isUserVerified(userId, communityId);
        } catch (Exception e) {
            log.error("检查发帖权限失败: userId={}, communityId={}", userId, communityId, e);
            return false;
        }
    }
}
