package syncer.syncerpluswebapp.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluswebapp.util.TokenUtils;
import syncer.syncerservice.util.jedis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/6
 */
@Slf4j
@Component
public class LogOutFilter implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {


        String token=request.getHeader("X-Token");

        if(!StringUtils.isEmpty(token)){
            TokenUtils.delToken(token);
        }

        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }

}
