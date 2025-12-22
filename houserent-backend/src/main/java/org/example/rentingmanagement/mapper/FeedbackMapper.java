package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.rentingmanagement.entity.Feedback;

/**
 * 用户反馈Mapper接口
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
