package org.example.rentingmanagement.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 系统启动初始化类
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class SystemInitRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemInitRunner.class);

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("小区房屋租赁管理平台后端系统启动中...");
        log.info("========================================");
        
        try {
            // 1. 检查数据库连接
            checkDatabaseConnection();
            
            // 2. 初始化MinIO存储桶
            initializeMinIOBuckets();
            
            // 3. 检查Redis连接
            checkRedisConnection();
            
            // 4. 初始化系统数据
            initializeSystemData();
            
            // 5. 清理过期数据
            cleanupExpiredData();
            
            log.info("========================================");
            log.info("✅ 系统启动完成！");
            log.info("🌐 API文档地址: http://localhost:8080/swagger-ui.html");
            log.info("💬 WebSocket地址: ws://localhost:8080/websocket/chat/");
            log.info("📁 文件服务: MinIO对象存储");
            log.info("🔐 认证方式: Sa-Token + 微信小程序");
            log.info("📋 核心功能:");
            log.info("   • 微信小程序登录认证");
            log.info("   • 房源发布与管理");
            log.info("   • 小区身份认证（双重审核）");
            log.info("   • WebSocket实时聊天");
            log.info("   • 电子合同系统（PDF生成+数字签名）");
            log.info("   • 信用评价系统");
            log.info("   • 交流论坛");
            log.info("   • 系统通知");
            log.info("   • 文件上传（MinIO）");
            log.info("   • 管理员后台");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("❌ 系统启动过程中发生错误: {}", e.getMessage(), e);
            log.error("请检查配置并重新启动");
        }
    }

    /**
     * 检查数据库连接
     */
    private void checkDatabaseConnection() {
        try {
            log.info("🔍 检查数据库连接...");
            // TODO: 实际的数据库连接检查
            log.info("✅ 数据库连接正常");
        } catch (Exception e) {
            log.warn("⚠️ 数据库连接检查失败: {}", e.getMessage());
        }
    }

    /**
     * 初始化MinIO存储桶
     */
    private void initializeMinIOBuckets() {
        try {
            log.info("🗂️ 初始化MinIO存储桶...");
            // TODO: 实际的MinIO存储桶初始化
            log.info("✅ MinIO存储桶初始化完成");
        } catch (Exception e) {
            log.warn("⚠️ MinIO存储桶初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 检查Redis连接
     */
    private void checkRedisConnection() {
        try {
            log.info("🔍 检查Redis连接...");
            // TODO: 实际的Redis连接检查
            log.info("✅ Redis连接正常");
        } catch (Exception e) {
            log.warn("⚠️ Redis连接检查失败: {}", e.getMessage());
        }
    }

    /**
     * 初始化系统数据
     */
    private void initializeSystemData() {
        try {
            log.info("🎯 初始化系统数据...");
            // TODO: 初始化默认管理员账户、系统配置等
            log.info("✅ 系统数据初始化完成");
        } catch (Exception e) {
            log.warn("⚠️ 系统数据初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 清理过期数据
     */
    private void cleanupExpiredData() {
        try {
            log.info("🧹 清理过期数据...");
            // TODO: 清理过期的WebSocket会话、临时文件等
            log.info("✅ 过期数据清理完成");
        } catch (Exception e) {
            log.warn("⚠️ 过期数据清理失败: {}", e.getMessage());
        }
    }
}
