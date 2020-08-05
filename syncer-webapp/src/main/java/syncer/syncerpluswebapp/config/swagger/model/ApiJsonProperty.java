package syncer.syncerpluswebapp.config.swagger.model;

/**
 * Created by yueh on 2018/9/7.
 */


import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiJsonProperty {

    String name();

    String defaultValue() default "";

    String description() default "";

    String allowableValues() default "";

    boolean required() default false;

    String access() default "";

    boolean allowMultiple() default false;

    Class<?> type() default String.class ;

    String paramType() default "";

    String example() default "";

    Example examples() default @Example(value = @ExampleProperty(mediaType = "", value = ""));


    String format() default "";

    boolean readOnly() default false;

    String collectionFormat() default "";

}