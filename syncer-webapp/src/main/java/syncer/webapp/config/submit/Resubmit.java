package syncer.webapp.config.submit;

import java.lang.annotation.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Resubmit {

    /**
     * 延时时间 在延时多久后可以再次提交
     *
     * @return Time unit is one second
     */
    int delaySeconds() default 20;
}
