package org.example.rentingmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 服务器配置类 - 自动获取本机IP
 */
@Slf4j
@Component
@Configuration
public class ServerConfig {

    @Value("${server.port:8888}")
    private Integer serverPort;

    @Value("${api.server.url:auto}")
    private String configuredUrl;

    private String serverUrl;

    @PostConstruct
    public void init() {
        log.info("=== ServerConfig 初始化 ===");
        log.info("配置的URL: {}", configuredUrl);
        log.info("服务器端口: {}", serverPort);
        
        if ("auto".equals(configuredUrl) || configuredUrl == null || configuredUrl.isEmpty()) {
            // 自动获取本地IP
            String localIp = getLocalIpAddress();
            this.serverUrl = "http://" + localIp + ":" + serverPort;
            log.info("✅ 自动检测到服务器地址: {}", this.serverUrl);
        } else {
            // 使用配置文件中的地址
            this.serverUrl = configuredUrl;
            log.info("✅ 使用配置的服务器地址: {}", this.serverUrl);
        }
        log.info("========================");
    }

    /**
     * 获取服务器URL
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * 自动获取本机局域网IP地址
     * 优先获取192.168.x.x或172.16-31.x.x段的IP
     */
    private String getLocalIpAddress() {
        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            String fallbackIp = "localhost";
            
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    
                    // 只处理IPv4地址
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1) {
                        String ip = inetAddress.getHostAddress();
                        
                        // 优先使用192.168.x.x段的IP（常见的局域网地址）
                        if (ip.startsWith("192.168.")) {
                            log.info("检测到局域网IP: {}", ip);
                            return ip;
                        }
                        
                        // 其次使用172.16-31.x.x段的IP
                        if (ip.startsWith("172.")) {
                            String[] parts = ip.split("\\.");
                            if (parts.length == 4) {
                                int secondOctet = Integer.parseInt(parts[1]);
                                if (secondOctet >= 16 && secondOctet <= 31) {
                                    log.info("检测到局域网IP: {}", ip);
                                    return ip;
                                }
                            }
                        }
                        
                        // 记录第一个非回环IPv4地址作为备用
                        if ("localhost".equals(fallbackIp)) {
                            fallbackIp = ip;
                        }
                    }
                }
            }
            
            // 如果没有找到局域网IP，使用备用IP
            if (!"localhost".equals(fallbackIp)) {
                log.info("使用备用IP: {}", fallbackIp);
                return fallbackIp;
            }
            
            // 最后的备选方案：使用InetAddress.getLocalHost()
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            log.info("使用默认IP: {}", ip);
            return ip;
            
        } catch (Exception e) {
            log.error("获取本地IP地址失败，使用localhost", e);
            return "localhost";
        }
    }
}
