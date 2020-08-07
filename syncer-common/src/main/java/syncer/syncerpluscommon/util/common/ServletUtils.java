package syncer.syncerpluscommon.util.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/6
 */
public class ServletUtils {
    static ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    public static HttpServletRequest request(){
        return  servletRequestAttributes.getRequest();
    }

    public static HttpServletResponse response(){
        return  servletRequestAttributes.getResponse();
    }
}
