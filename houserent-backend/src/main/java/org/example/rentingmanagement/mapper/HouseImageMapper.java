package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.rentingmanagement.entity.HouseImage;

/**
 * 房源图片Mapper接口
 */
@Mapper
public interface HouseImageMapper extends BaseMapper<HouseImage> {
    // MyBatis Plus BaseMapper已提供基本CRUD操作
    // 复杂查询在Service层使用QueryWrapper实现
}
