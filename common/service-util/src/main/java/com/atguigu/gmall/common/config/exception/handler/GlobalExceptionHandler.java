package com.atguigu.gmall.common.config.exception.handler;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice //通知
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errMap = bindingResult.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return Result.build(errMap, ResultCodeEnum.INVALID_PARAM);
    }

    /**
     * 处理所有业务相关的异常 GmallException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(GmallException.class)
    public Result handGmallException(GmallException e) {
        Result<Object> fail = Result.fail();
        fail.setCode(e.getCode());
        fail.setMessage(e.getMessage());
        return fail;
    }

    /**
     * 处理所有Controller出现的其他异常
     *
     * @param e
     * @return
     */
//    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("Exception: {}", e);
        Result<Object> fail = Result.fail();
        fail.setMessage(e.getMessage());
        return fail;
    }
}
