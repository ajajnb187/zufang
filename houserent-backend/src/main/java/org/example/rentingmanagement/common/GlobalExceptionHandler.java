package org.example.rentingmanagement.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理Sa-Token未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        return Result.unauthorized("请先登录");
    }

    /**
     * 处理Sa-Token权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<String> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足异常: {}", e.getMessage());
        return Result.forbidden("权限不足");
    }

    /**
     * 处理参数校验异常 - @Valid注解
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidException(MethodArgumentNotValidException e) {
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getDefaultMessage()).append("; ");
        }
        log.warn("参数校验异常: {}", errorMsg);
        return Result.paramError(errorMsg.toString());
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getDefaultMessage()).append("; ");
        }
        log.warn("参数绑定异常: {}", errorMsg);
        return Result.paramError(errorMsg.toString());
    }

    /**
     * 处理参数类型异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型异常: {}", e.getMessage());
        return Result.paramError("参数类型错误");
    }

    /**
     * 处理约束校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder errorMsg = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            errorMsg.append(violation.getMessage()).append("; ");
        }
        log.warn("约束校验异常: {}", errorMsg);
        return Result.paramError(errorMsg.toString());
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限: {}", e.getMessage());
        return Result.error(ResultCode.FILE_SIZE_EXCEEDED, "文件大小超出限制");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.error("系统内部错误");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return Result.paramError(e.getMessage());
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请联系管理员");
    }
}
