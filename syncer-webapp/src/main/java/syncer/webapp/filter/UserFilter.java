package syncer.webapp.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import syncer.common.entity.ResponseResult;
import syncer.transmission.util.strings.StringUtils;
import syncer.transmission.util.token.TokenUtils;
import syncer.webapp.util.TokenNameUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/6
 */
@Slf4j
@Component
public class UserFilter implements HandlerInterceptor {
    @Autowired
    private Environment env;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String tokenFilterStatus=env.getProperty("syncer.config.auth");
        String swaggerFilterStatus=env.getProperty("syncer.config.swagger");
        if("false".equalsIgnoreCase(swaggerFilterStatus)){
            String url=request.getRequestURI();
            if(url.equalsIgnoreCase("/csrf")||url.equalsIgnoreCase("/error")||url.contains("/swagger-ui.html")||url.contains("/webjars/")||url.contains("/swagger-resources")||url.contains("/swagger-ui.html/swagger-resources/")||url.equalsIgnoreCase("/v2/api-docs")){
                return true;
            }
        }
        if("false".equalsIgnoreCase(tokenFilterStatus)){
            return true;
        }
        String token=request.getHeader(TokenNameUtils.TOKEN_NAME);
        if(StringUtils.isEmpty(token)){
            response.setStatus(403);
            response.setContentType("application/json; charset=utf-8");
            returnJson(response, ResponseResult.builder().code("100").msg("no token").build().json());
            return false;
        }else{
            if(!TokenUtils.checkToken(token)){
                response.setStatus(403);
                returnJson(response,ResponseResult.builder().code("100").msg("no token").build().json());
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }



    private void returnJson(HttpServletResponse response, String json){
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
            log.error("response error",e);
        } finally {
            if (writer != null){
                writer.close();
            }

        }
    }
}
