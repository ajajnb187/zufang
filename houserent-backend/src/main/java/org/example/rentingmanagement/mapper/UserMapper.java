package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据openid查询用户
     */
    @Select("SELECT * FROM users WHERE openid = #{openid} AND status != 'deleted'")
    User findByOpenid(String openid);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND status != 'deleted'")
    User findByPhone(String phone);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND status != 'deleted'")
    User findByUsername(String username);
}
