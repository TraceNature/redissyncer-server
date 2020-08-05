package syncer.syncerpluswebapp.config.swagger.model;

/**
 * Created by yueh on 2018/9/7.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiJsonObject{

    ApiJsonProperty[] value(); //对象属性值

    ApiJsonResult result() default @ApiJsonResult({});


    String name() default "";

}
