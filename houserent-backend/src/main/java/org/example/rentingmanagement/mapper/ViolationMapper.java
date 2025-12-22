package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.rentingmanagement.entity.Violation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 违规记录Mapper接口
 */
@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {

    @Select("SELECT violation_type as type, COUNT(*) as count FROM violations GROUP BY violation_type")
    List<Map<String, Object>> countByType();

    @Select("SELECT status, COUNT(*) as count FROM violations GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT * FROM violations WHERE created_at >= #{startTime} ORDER BY created_at DESC")
    List<Violation> findRecentViolations(@Param("startTime") LocalDateTime startTime);

    @Select("SELECT COUNT(*) FROM violations WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM violations WHERE message_id = #{messageId}")
    int countByMessageId(@Param("messageId") Long messageId);
}