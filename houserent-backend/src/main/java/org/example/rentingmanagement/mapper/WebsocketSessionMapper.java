package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.WebsocketSession;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * WebSocket会话管理数据访问层
 */
@Mapper
public interface WebsocketSessionMapper extends BaseMapper<WebsocketSession> {

    /**
     * 根据用户ID查询会话
     */
    @Select("SELECT * FROM websocket_sessions WHERE user_id = #{userId} AND status = 'online'")
    WebsocketSession findByUserId(Long userId);

    /**
     * 查询在线用户会话
     */
    @Select("SELECT * FROM websocket_sessions WHERE status = 'online'")
    List<WebsocketSession> findOnlineSessions();

    /**
     * 更新会话状态
     */
    @Update("UPDATE websocket_sessions SET status = #{status}, updated_at = NOW() WHERE session_id = #{sessionId}")
    void updateStatus(String sessionId, String status);

    /**
     * 更新心跳时间
     */
    @Update("UPDATE websocket_sessions SET last_heartbeat = NOW(), updated_at = NOW() WHERE session_id = #{sessionId}")
    void updateHeartbeat(String sessionId);

    /**
     * 清理离线会话（超过指定时间未心跳）
     */
    @Update("UPDATE websocket_sessions SET status = 'offline' WHERE last_heartbeat < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE)")
    void cleanupOfflineSessions(int timeoutMinutes);

    /**
     * 根据用户ID删除会话
     */
    @Delete("DELETE FROM websocket_sessions WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);
}
