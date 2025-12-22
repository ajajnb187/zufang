package org.example.rentingmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 基于SpringBoot 3.2最新WebSocket实现方案
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    /**
     * 注入ServerEndpointExporter
     * 这个Bean会自动注册使用了@ServerEndpoint注解声明的WebSocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
