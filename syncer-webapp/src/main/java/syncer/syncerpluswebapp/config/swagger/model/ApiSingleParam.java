package syncer.syncerpluswebapp.config.swagger.model;

/**
 * Created by yueh on 2018/9/7.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSingleParam {

    String name() default "";

    String value() default "";

    Class<?> type() default String.class;

    String example() default "";

    boolean allowMultiple() default false;

}
