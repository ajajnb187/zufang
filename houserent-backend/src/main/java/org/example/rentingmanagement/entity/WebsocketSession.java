package org.example.rentingmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * WebSocket会话管理实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("websocket_sessions")
public class WebsocketSession {

    @TableId(value = "session_id", type = IdType.INPUT)
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("server_node")
    private String serverNode;

    @TableField("status")
    private String status; // online, offline

    @TableField("last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
