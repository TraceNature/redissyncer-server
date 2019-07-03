package com.i1314i.syncerpluswebapp.advice;

import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerpluswebapp.constant.CodeConstant;
import com.i1314i.syncerpluswebapp.constant.HttpMsgConstant;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常捕获处理
 **/
@RestControllerAdvice
public class ExceptionAdvice {
    /**
     * 500错误请求 信息解析错误
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResultMap IllegalArgumentException(){
        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
                .msg(HttpMsgConstant.HTTP_ERROR_MESSAGE);
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
