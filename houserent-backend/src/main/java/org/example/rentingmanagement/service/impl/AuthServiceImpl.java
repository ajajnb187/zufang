package org.example.rentingmanagement.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.entity.Admin;
import org.example.rentingmanagement.entity.Community;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.AdminMapper;
import org.example.rentingmanagement.mapper.CommunityMapper;
import org.example.rentingmanagement.mapper.CommunityVerificationMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.AuthService;
import org.example.rentingmanagement.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 认证服务实现类
 * 基于Sa-Token 1.44.0最新版本实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    private final CommunityVerificationMapper communityVerificationMapper;
    private final AdminMapper adminMapper;
    private final CommunityMapper communityMapper;

    @Value("${wechat.appid}")
    private String wechatAppId;

    @Value("${wechat.secret}")
    private String wechatSecret;

    /**
     * 微信API获取session_key和openid的URL
     */
    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @Override
    public Object adminLogin(String username, String password) {
        try {
            // 1. 通过手机号查询用户（使用username作为手机号）
            User user = baseMapper.findByPhone(username);
            if (user == null) {
                throw new BusinessException(ResultCode.LOGIN_ERROR, "用户名或密码错误");
            }

            // 2. 验证密码
            if (!PasswordUtils.matches(password, user.getPassword())) {
                throw new BusinessException(ResultCode.LOGIN_ERROR, "用户名或密码错误");
            }

            // 3. 检查用户状态
            if ("banned".equals(user.getStatus())) {
                throw new BusinessException(ResultCode.ACCOUNT_BANNED, "账号已被封禁");
            }

            // 4. 查询管理员信息
            Admin admin = adminMapper.findByUserId(user.getUserId());
            if (admin == null || !"active".equals(admin.getStatus())) {
                throw new BusinessException(ResultCode.PERMISSION_DENIED, "权限不足");
            }

            // 5. 使用Sa-Token进行登录
            StpUtil.login(user.getUserId());

            // 6. SaToken会通过StpInterface自动获取角色，无需手动设置

            // 7. 构建返回数据
            JSONObject result = new JSONObject();
            result.set("token", StpUtil.getTokenValue());
            result.set("userId", user.getUserId());
            result.set("username", user.getPhone()); // 使用手机号作为用户名
            result.set("nickname", user.getNickname());
            result.set("adminType", admin.getAdminType());
            result.set("communityId", admin.getCommunityId());
            
            // 获取小区名称
            if (admin.getCommunityId() != null) {
                Community community = communityMapper.selectById(admin.getCommunityId());
                if (community != null) {
                    result.set("communityName", community.getCommunityName());
                }
            }

            // 调试日志：检查Admin数据完整性
            log.info("Admin数据: adminType={}, communityId={}, userId={}",
                    admin.getAdminType(), admin.getCommunityId(), user.getUserId());

            if ("community".equals(admin.getAdminType()) && admin.getCommunityId() == null) {
                log.warn("小区管理员{}缺少communityId，请检查admin表数据", user.getPhone());
                result.set("warning", "账号数据不完整：缺少小区关联信息");
            }

            log.info("管理员{}登录成功", user.getPhone());
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员登录异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.LOGIN_ERROR, "登录失败");
        }
    }

    @Override
    public String adminRegister(String phone, String password, String adminType, Long communityId) {
        try {
            // 1. 检查手机号是否已存在
            User existUser = baseMapper.findByPhone(phone);
            if (existUser != null) {
                throw new BusinessException(ResultCode.USER_EXISTS, "手机号已存在");
            }

            // 2. 创建用户记录
            User user = new User();
            user.setOpenid("admin_" + phone); // 管理员使用特殊openid格式，区分微信用户
            user.setPhone(phone); // 使用手机号
            user.setPassword(PasswordUtils.encode(password)); // 使用BCrypt加密密码
            user.setNickname("管理员_" + phone);
            user.setStatus("active");
            user.setUserType(0); // 0表示管理员用户
            user.setCreditScore(BigDecimal.valueOf(5.0));

            log.info("准备保存用户: phone={}, openid={}, nickname={}", user.getPhone(), user.getOpenid(), user.getNickname());

            // 3. 保存用户
            boolean saved = this.save(user);
            log.info("用户保存结果: {}, userId={}, phone={}", saved, user.getUserId(), user.getPhone());

            // 4. 创建管理员记录
            Admin admin = new Admin();
            admin.setUserId(user.getUserId());
            admin.setAdminType(adminType);
            admin.setCommunityId(communityId);
            admin.setStatus("active");
            admin.setPermissions("{}"); // 默认空权限配置

            // 5. 保存管理员记录
            adminMapper.insert(admin);

            log.info("管理员注册成功: phone={}, adminType={}, userId={}", user.getPhone(), adminType, user.getUserId());
            return "注册成功";

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员注册异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.REGISTER_ERROR, "注册失败");
        }
    }


    @Override
    public String wechatLogin(String code) {
        try {
            // 1. 调用微信API换取openid
            String url = String.format(WECHAT_LOGIN_URL, wechatAppId, wechatSecret, code);
            String response = HttpUtil.get(url);

            log.info("微信登录响应: {}", response);

            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 检查错误
            if (jsonObject.containsKey("errcode")) {
                int errcode = jsonObject.getInt("errcode");
                if (errcode != 0) {
                    String errmsg = jsonObject.getStr("errmsg");
                    log.error("微信登录失败: {}", errmsg);
                    throw new BusinessException(ResultCode.WECHAT_AUTH_ERROR, "微信登录失败: " + errmsg);
                }
            }

            String openid = jsonObject.getStr("openid");
            if (StrUtil.isBlank(openid)) {
                throw new BusinessException(ResultCode.WECHAT_AUTH_ERROR, "获取openid失败");
            }

            // 2. 查询或创建用户
            User user = baseMapper.findByOpenid(openid);
            if (user == null) {
                // 新用户自动注册
                user = new User();
                user.setOpenid(openid);
                user.setNickname("微信用户");
                user.setStatus("active");
                user.setUserType(3); // 3=租客，默认身份
                user.setCreditScore(BigDecimal.valueOf(5.0));
                this.save(user);
                log.info("新用户注册: userId={}, openid={}, userType=3(租客)", user.getUserId(), openid);
            } else {
                // 检查账号状态
                if ("banned".equals(user.getStatus())) {
                    throw new BusinessException(ResultCode.ACCOUNT_BANNED, "账号已被封禁");
                }
            }

            // 3. 使用Sa-Token登录
            StpUtil.login(user.getUserId());

            log.info("用户{}登录成功", user.getUserId());

            return StpUtil.getTokenValue();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.WECHAT_AUTH_ERROR, "微信登录失败");
        }
    }
    @Override
    public String refreshToken() {
        // Sa-Token会自动刷新token，这里返回当前token
        StpUtil.checkLogin();
        return StpUtil.getTokenValue();
    }

    @Override
    public void logout() {
        StpUtil.logout();
        log.info("用户{}退出登录", StpUtil.getLoginIdDefaultNull());
    }

    @Override
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        return user;
    }

    @Override
    public boolean isUserVerifiedInCommunity(Long userId, Long communityId) {
        return communityVerificationMapper.isUserVerified(userId, communityId);
    }

    /**
     * 注册新用户
     */
    private User registerNewUser(String openid, String encryptedData, String iv, String sessionKey, Integer userType) {
        User user = new User();
        user.setOpenid(openid);
        user.setStatus("active");
        user.setCreditScore(BigDecimal.valueOf(5.0)); // 默认信用分5分
        user.setUserType(userType != null ? userType : 3); // 默认为租户

        // 解析用户信息（如果提供了加密数据）
        if (StrUtil.isNotBlank(encryptedData) && StrUtil.isNotBlank(iv)) {
            parseUserInfo(user, encryptedData, iv, sessionKey);
        }

        // 保存用户
        this.save(user);

        log.info("新用户注册成功: userId={}, openid={}, userType={}", user.getUserId(), openid, user.getUserType());

        return user;
    }

    /**
     * 更新用户信息
     */
    private void updateUserInfo(User user, String encryptedData, String iv, String sessionKey) {
        parseUserInfo(user, encryptedData, iv, sessionKey);
        this.updateById(user);
    }

    /**
     * 解析微信用户信息
     */
    private void parseUserInfo(User user, String encryptedData, String iv, String sessionKey) {
        try {
            // TODO: 实现微信加密数据解密
            // 这里需要使用微信提供的解密算法来解密encryptedData
            // 由于涉及加密解密，这里暂时跳过具体实现
            // 在实际项目中，可以使用微信官方提供的SDK或第三方库来实现

            log.info("解析用户信息: userId={}", user.getUserId());

            // 暂时设置默认值
            if (StrUtil.isBlank(user.getNickname())) {
                user.setNickname("微信用户");
            }

        } catch (Exception e) {
            log.error("解析微信用户信息失败: {}", e.getMessage(), e);
            // 解析失败不影响登录
        }
    }

    /**
     * 设置用户角色
     */
    private void setUserRole(User user) {
        // 根据用户类型设置Sa-Token角色
        switch (user.getUserType()) {
            case 3:
                StpUtil.getSession().set("role", "tenant");
                break;
            case 4:
                StpUtil.getSession().set("role", "landlord");
                break;
            default:
                StpUtil.getSession().set("role", "user");
        }

        // SaToken会通过StpInterface自动获取角色，无需手动设置
    }

}
