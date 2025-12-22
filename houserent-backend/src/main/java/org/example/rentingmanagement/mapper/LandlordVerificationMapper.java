package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.rentingmanagement.entity.LandlordVerification;

/**
 * 房东认证Mapper
 */
@Mapper
public interface LandlordVerificationMapper extends BaseMapper<LandlordVerification> {
    // MyBatis Plus BaseMapper已提供基本CRUD操作
    // 复杂查询在Service层使用QueryWrapper实现
}
