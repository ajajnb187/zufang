package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.CommunityVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 小区认证数据访问层
 */
@Mapper
public interface CommunityVerificationMapper extends BaseMapper<CommunityVerification> {

    /**
     * 查询用户在某小区的认证记录
     */
    @Select("SELECT * FROM community_verifications WHERE user_id = #{userId} AND community_id = #{communityId} ORDER BY created_at DESC LIMIT 1")
    CommunityVerification findByUserAndCommunity(Long userId, Long communityId);

    /**
     * 查询待审核的认证申请（小区管理员）- 包含用户信息
     */
    @Select("SELECT cv.*, u.nickname as user_nickname, u.phone as user_phone, u.avatar_url as user_avatar, " +
            "c.community_name as community_name " +
            "FROM community_verifications cv " +
            "LEFT JOIN users u ON cv.user_id = u.user_id " +
            "LEFT JOIN communities c ON cv.community_id = c.community_id " +
            "WHERE cv.community_id = #{communityId} AND cv.community_admin_status = 'pending' " +
            "ORDER BY cv.created_at ASC")
    List<Map<String, Object>> findPendingByCommunityWithUserInfo(Long communityId);
    
    /**
     * 查询小区所有认证申请（包含所有状态）- 包含用户信息
     */
    @Select("SELECT cv.*, u.nickname as user_nickname, u.phone as user_phone, u.avatar_url as user_avatar, " +
            "c.community_name as community_name " +
            "FROM community_verifications cv " +
            "LEFT JOIN users u ON cv.user_id = u.user_id " +
            "LEFT JOIN communities c ON cv.community_id = c.community_id " +
            "WHERE cv.community_id = #{communityId} " +
            "ORDER BY cv.created_at DESC")
    List<Map<String, Object>> findAllByCommunityWithUserInfo(Long communityId);

    /**
     * 查询待审核的认证申请（小区管理员）
     */
    @Select("SELECT * FROM community_verifications WHERE community_id = #{communityId} AND community_admin_status = 'pending' ORDER BY created_at ASC")
    List<CommunityVerification> findPendingByCommunity(Long communityId);

    /**
     * 查询待平台审核的认证申请 - 包含用户信息
     */
    @Select("SELECT cv.*, u.nickname as user_nickname, u.phone as user_phone, u.avatar_url as user_avatar, " +
            "c.community_name as community_name " +
            "FROM community_verifications cv " +
            "LEFT JOIN users u ON cv.user_id = u.user_id " +
            "LEFT JOIN communities c ON cv.community_id = c.community_id " +
            "WHERE cv.community_admin_status = 'approved' AND cv.platform_admin_status = 'pending' " +
            "ORDER BY cv.created_at ASC")
    List<Map<String, Object>> findPendingForPlatformWithUserInfo();

    /**
     * 查询待平台审核的认证申请
     */
    @Select("SELECT * FROM community_verifications WHERE community_admin_status = 'approved' AND platform_admin_status = 'pending' ORDER BY created_at ASC")
    List<CommunityVerification> findPendingForPlatform();

    /**
     * 检查用户是否已通过某小区认证
     */
    @Select("SELECT COUNT(*) > 0 FROM community_verifications WHERE user_id = #{userId} AND community_id = #{communityId} AND final_status = 'approved'")
    boolean isUserVerified(Long userId, Long communityId);
}
