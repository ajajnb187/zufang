package org.example.rentingmanagement.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 使用Spring Security BCrypt加密（符合OWASP安全标准）
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 密码加密
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        // 使用BCrypt加密，符合OWASP安全标准
        return encoder.encode(rawPassword);
    }

    /**
     * 密码验证
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 验证结果
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        // BCrypt密码验证
        return encoder.matches(rawPassword, encodedPassword);
    }
}
