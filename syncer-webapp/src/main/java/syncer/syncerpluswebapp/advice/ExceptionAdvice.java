package syncer.syncerpluswebapp.advice;


import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerpluswebapp.constant.CodeConstant;
import syncer.syncerpluswebapp.constant.HttpMsgConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统一异常捕获处理
 **/
@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {


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
        log.warn(e.getMessage());
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
        log.warn(e.getMessage());
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
        log.warn(e.getMessage());
        String msg = msgConvertor(((MethodArgumentNotValidException) e).getBindingResult());
        return ResultMap.builder().code(CodeConstant.VALITOR_ERROR_CODE)
                .msg(msg);
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
    public ResultMap TaskMsgException(TaskMsgException e){
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
    public ResultMap HttpMessageNotReadableException(HttpMessageNotReadableException e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.HTTP_MSG_PARSE_ERROR_CODE)
                .msg(HttpMsgConstant.HTTP_MSG_PARSE_ERROR_CODE);
    }

    /**
     * 500错误请求 信息解析错误
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResultMap Exception(Exception e){
        log.warn(e.getMessage());
        return ResultMap.builder().code(CodeConstant.HTTP_ERROR_CODE)
                .msg(HttpMsgConstant.HTTP_ERROR_MESSAGE);
    }
}
