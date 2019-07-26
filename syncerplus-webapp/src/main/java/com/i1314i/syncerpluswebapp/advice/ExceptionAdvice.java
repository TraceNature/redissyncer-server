package com.i1314i.syncerpluswebapp.advice;


import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerpluswebapp.constant.CodeConstant;
import com.i1314i.syncerpluswebapp.constant.HttpMsgConstant;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一异常捕获处理
 **/
@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {
    /**
     * 500错误请求 信息解析错误
     * @return
     */
//    @ExceptionHandler(Exception.class)
//    public ResultMap Exception(){
//        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
//                .msg(HttpMsgConstant.HTTP_ERROR_MESSAGE);
//    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultMap IllegalArgumentException(IllegalArgumentException e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage());
    }
    @ExceptionHandler(AssertionError.class)
    public ResultMap IllegalArgumentException(AssertionError e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage());
    }


    @ExceptionHandler(SocketTimeoutException.class)
    public ResultMap SocketTimeoutException(SocketTimeoutException e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage());
    }

    @ExceptionHandler(ConnectException.class)
    public ResultMap ConnectException(ConnectException e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage());
    }




    /**
     * valitor校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResultMap ConstraintViolationException(ConstraintViolationException e){
        List<ConstraintViolation> errorInformation = new ArrayList<ConstraintViolation>(e.getConstraintViolations());
        return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(errorInformation.get(0).getMessage());
    }

    /**
     * valitor校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public ResultMap BindException(BindException e){
        List<String> errorInformation = e.getBindingResult().getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(errorInformation.get(0));
    }



    /**
     * valitor校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultMap MethodArgumentNotValidException(MethodArgumentNotValidException e){
        String message = e.getMessage();
        return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(message);
    }


    /**
     * 线程信息异常
     * @param e
     * @return
     */

    @ExceptionHandler(TaskMsgException.class)
    public ResultMap TaskMsgException(TaskMsgException e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(e.getMessage());
    }


    /**
     * 400错误请求 信息解析错误
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultMap HttpMessageNotReadableException(){
        return ResultMap.builder().code(CodeConstant.HTTP_MSG_PARSE_ERROR_CODE)
                .msg(HttpMsgConstant.HTTP_MSG_PARSE_ERROR_CODE);
    }

}
