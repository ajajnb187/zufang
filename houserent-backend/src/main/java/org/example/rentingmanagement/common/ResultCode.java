package org.example.rentingmanagement.common;

/**
 * 响应码常量类
 */
public class ResultCode {
    
    /**
     * 成功
     */
    public static final Integer SUCCESS = 200;
    
    /**
     * 失败
     */
    public static final Integer ERROR = 500;
    
    /**
     * 参数错误
     */
    public static final Integer PARAM_ERROR = 400;
    
    /**
     * 未认证
     */
    public static final Integer UNAUTHORIZED = 401;
    
    /**
     * 无权限
     */
    public static final Integer FORBIDDEN = 403;
    
    /**
     * 资源不存在
     */
    public static final Integer NOT_FOUND = 404;
    
    /**
     * 请求方法不支持
     */
    public static final Integer METHOD_NOT_ALLOWED = 405;
    
    /**
     * 用户不存在
     */
    public static final Integer USER_NOT_FOUND = 1001;
    
    /**
     * 用户已存在
     */
    public static final Integer USER_EXISTS = 1002;
    
    /**
     * 密码错误
     */
    public static final Integer PASSWORD_ERROR = 1003;
    
    /**
     * 账号被封禁
     */
    public static final Integer ACCOUNT_BANNED = 1004;
    
    /**
     * 微信授权失败
     */
    public static final Integer WECHAT_AUTH_ERROR = 1005;

    /**
     * 登录失败
     */
    public static final Integer LOGIN_ERROR = 1006;

    /**
     * 注册失败
     */
    public static final Integer REGISTER_ERROR = 1007;

    /**
     * 权限不足
     */
    public static final Integer PERMISSION_DENIED = 1008;

    /**
     * 参数无效
     */
    public static final Integer INVALID_PARAM = 1009;
    
    /**
     * 小区认证未通过
     */
    public static final Integer COMMUNITY_NOT_VERIFIED = 2001;
    
    /**
     * 房源不存在
     */
    public static final Integer HOUSE_NOT_FOUND = 3001;
    
    /**
     * 房源已下架
     */
    public static final Integer HOUSE_OFFLINE = 3002;
    
    /**
     * 合同不存在
     */
    public static final Integer CONTRACT_NOT_FOUND = 4001;
    
    /**
     * 合同状态错误
     */
    public static final Integer CONTRACT_STATUS_ERROR = 4002;
    
    /**
     * 文件上传失败
     */
    public static final Integer FILE_UPLOAD_ERROR = 5001;
    
    /**
     * 文件类型不支持
     */
    public static final Integer FILE_TYPE_NOT_SUPPORTED = 5002;
    
    /**
     * 文件大小超限
     */
    public static final Integer FILE_SIZE_EXCEEDED = 5003;
    
    /**
     * 操作失败
     */
    public static final Integer OPERATION_FAILED = 6001;
    
    /**
     * 参数无效
     */
    public static final Integer INVALID_PARAMETER = 6002;
}
