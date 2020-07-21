package syncer.syncerpluswebapp.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.service.Parameter;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/6/8
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {

        ParameterBuilder aParameterBuilder = new ParameterBuilder();
        aParameterBuilder.name("Syncer-Token").description("登陆令牌").modelRef(new ModelRef("string")).parameterType("header").required(true).build();


        List<Parameter> aParameters = Lists.newArrayList();
        aParameters.add(aParameterBuilder.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .globalOperationParameters(aParameters)
                .select()
                .apis(RequestHandlerSelectors.basePackage("syncer.syncerpluswebapp.controller.v2"))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("Redis Syncer Swagger")
                        .description("Redis Syncer Swagger")
                        .version("3.0")
//                        .contact( new Contact("啊啊啊啊","blog.csdn.net","aaa@gmail.com"))
//                        .license("The Apache License")
//                        .licenseUrl("http://www.jdcloud.com")
                        .build());
    }
}
