package org.example.rentingmanagement.common;

import lombok.Data;

/**
 * 通用响应结果类
 */
@Data
public class Result<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS, "操作成功");
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, "操作成功", data);
    }
    
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS, message);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR, "操作失败");
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR, message);
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }
    
    /**
     * 参数错误
     */
    public static <T> Result<T> paramError() {
        return new Result<>(ResultCode.PARAM_ERROR, "参数错误");
    }
    
    public static <T> Result<T> paramError(String message) {
        return new Result<>(ResultCode.PARAM_ERROR, message);
    }
    
    /**
     * 未认证
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(ResultCode.UNAUTHORIZED, "未认证或登录已失效");
    }
    
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(ResultCode.UNAUTHORIZED, message);
    }
    
    /**
     * 无权限
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(ResultCode.FORBIDDEN, "权限不足");
    }
    
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(ResultCode.FORBIDDEN, message);
    }
}
