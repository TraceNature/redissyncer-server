// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.webapp.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import syncer.common.entity.ResponseResult;
import syncer.common.exception.TaskMsgException;
import syncer.transmission.exception.TaskErrorException;
import syncer.transmission.util.code.CodeUtils;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.constants.CodeConstant;
import syncer.webapp.constants.HttpMsgConstant;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 统一异常捕获处理
 * @Date 2020/12/8
 */
@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseResult IllegalArgumentException(IllegalArgumentException e){
        log.warn(e.getMessage());
        return ResponseResult.builder()
                .code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage())
                .build();
    }
    @ExceptionHandler(AssertionError.class)
    public ResponseResult IllegalArgumentException(AssertionError e){
        log.warn(e.getMessage());
        return ResponseResult.builder()
                .code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage())
                .build();
    }

    /**
     * 当前映射关系不存在
     * @param e
     * @return
     */
    @ExceptionHandler(TaskErrorException.class)
    public ResponseResult TaskErrorException(TaskErrorException e){
        log.warn(e.getMessage());
        return ResponseResult.builder()
                .code(ApiConstants.ERROR_CODE)
                .msg(e.getMessage())
                .build();
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseResult SocketTimeoutException(SocketTimeoutException e){
        log.warn(e.getMessage());
        return ResponseResult.builder()
                .code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage())
                .build();
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseResult ConnectException(ConnectException e){
        log.warn(e.getMessage());
        return ResponseResult.builder()
                .code(CodeConstant.HTTP_ERROR_CODE)
                .msg(e.getMessage())
                .build();
    }




    /**
     * valitor校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseResult ConstraintViolationException(ConstraintViolationException e){
        List<ConstraintViolation> errorInformation = new ArrayList<ConstraintViolation>(e.getConstraintViolations());
        return ResponseResult.builder()
                .code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(errorInformation.get(0).getMessage())
                .build();
    }

    /**
     * valitor校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public ResponseResult BindException(BindException e){
        List<String> errorInformation = e.getBindingResult().getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        return ResponseResult.builder()
                .code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(errorInformation.get(0))
                .build();
    }



    /**
     * valitor校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult MethodArgumentNotValidException(MethodArgumentNotValidException e){
        String msg = msgConvertor(((MethodArgumentNotValidException) e).getBindingResult());
        return ResponseResult.builder().code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(msg).build();
    }



    /*  数据校验处理 */
//    @ExceptionHandler({BindException.class, ConstraintViolationException.class})
//    public ResultMap validatorExceptionHandler(Exception e) {
//        String msg = e instanceof BindException ? msgConvertor(((BindException) e).getBindingResult())
//                : msgConvertor(((ConstraintViolationException) e).getConstraintViolations());
//
//                return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
//                .msg(msg);
//
//    }

    /**
     * 校验消息转换拼接
     *
     * @param bindingResult
     * @return
     */
    public static String msgConvertor(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        fieldErrors.forEach(fieldError -> sb.append(fieldError.getDefaultMessage()).append(","));

        return sb.deleteCharAt(sb.length() - 1).toString().toLowerCase();
    }

    private String msgConvertor(Set<ConstraintViolation<?>> constraintViolations) {
        StringBuilder sb = new StringBuilder();
        constraintViolations.forEach(violation -> sb.append(violation.getMessage()).append(","));

        return sb.deleteCharAt(sb.length() - 1).toString().toLowerCase();
    }



    /**
     * 线程信息异常
     * @param e
     * @return
     */

    @ExceptionHandler(TaskMsgException.class)
    public ResponseResult TaskMsgException(TaskMsgException e){
        log.warn(e.getMessage());
        return CodeUtils.codeMessages(e.getMessage());
    }

//    @ExceptionHandler(TaskMsgException.class)
//    public ResultMap TaskMsgException(TaskMsgException e){
//        log.warn(e.getMessage());
//        return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
//                .msg(e.getMessage());
//    }

    /**
     * 400错误请求 信息解析错误
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseResult HttpMessageNotReadableException(){
        return ResponseResult.builder()
                .code(CodeConstant.HTTP_MSG_PARSE_ERROR_CODE)
                .msg(HttpMsgConstant.HTTP_MSG_PARSE_ERROR_CODE)
                .build();
    }

    /**
     * 500错误请求 信息解析错误
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult Exception(){
        return ResponseResult.builder()
                .code(CodeConstant.HTTP_ERROR_CODE)
                .msg(HttpMsgConstant.HTTP_ERROR_MESSAGE)
                .build();
    }
}
