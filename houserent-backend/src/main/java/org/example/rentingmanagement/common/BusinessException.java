package org.example.rentingmanagement.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.ERROR;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 抛出业务异常
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    public static void throwIf(boolean condition, Integer code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }
}
