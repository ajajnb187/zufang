package org.example.rentingmanagement.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.config.ServerConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置接口 - 提供服务器配置信息
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ServerConfig serverConfig;

    /**
     * 获取服务器配置
     * 用于小程序自动获取后端地址
     */
    @GetMapping("/server")
    public Result<Map<String, String>> getServerConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("serverUrl", serverConfig.getServerUrl());
        
        log.info("小程序请求服务器配置: {}", serverConfig.getServerUrl());
        
        return Result.success(config);
    }
}
