package syncer.syncerpluswebapp.config.swagger.handler;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ResolvedTypes;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.DescriptionResolver;
import syncer.syncerpluswebapp.config.swagger.ModelCache;
import syncer.syncerpluswebapp.config.swagger.model.ApiJsonObject;
import syncer.syncerpluswebapp.config.swagger.model.ApiSingleParam;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static springfox.documentation.spi.schema.contexts.ModelContext.inputParam;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

/**
 * Created by yueh on 2018/9/10.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ParametersReader implements OperationBuilderPlugin {
    private final DescriptionResolver descriptions;


    private final TypeNameExtractor nameExtractor;
    private final TypeResolver resolver;

    @Autowired
    public ParametersReader(DescriptionResolver descriptions, TypeNameExtractor nameExtractor, TypeResolver resolver) {
        this.nameExtractor = nameExtractor;
        this.resolver = resolver;
        this.descriptions = descriptions;
    }

    @Override
    public void apply(OperationContext context) {
        context.operationBuilder().parameters(readParameters(context));
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return pluginDoesApply(delimiter);
    }

    private List<Parameter> readParameters(OperationContext context) {
        List<Parameter> parameters = Lists.newArrayList();
        List<ResolvedMethodParameter> methodParameters = context.getParameters();

        Map<String, ApiSingleParam> paramMap = new HashMap<>();
        Field[] fields = ModelCache.getInstance().getParamClass().getDeclaredFields();
        String type = new String();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ApiSingleParam.class)) {
                ApiSingleParam param = field.getAnnotation(ApiSingleParam.class);
                try {
                    String name = (String) field.get(type);
                    paramMap.put(name, param);
                } catch (Exception e) {
                }
            }
        }


        for (ResolvedMethodParameter methodParameter : methodParameters) {
            ParameterContext parameterContext = new ParameterContext(methodParameter,
                    new ParameterBuilder(),
                    context.getDocumentationContext(),
                    context.getGenericsNamingStrategy(),
                    context);
            Function<ResolvedType, ? extends ModelReference> factory = createModelRefFactory(parameterContext);
            Optional<ApiJsonObject> annotation = context.findAnnotation(ApiJsonObject.class);

            if (annotation.isPresent()) {
                ModelCache.getInstance().setFactory(factory)
                        .setParamMap(paramMap)
                        .addModel(annotation.get());

            }
        }

        return parameters;
    }

    private Function<ResolvedType, ? extends ModelReference> createModelRefFactory(ParameterContext context) {
        ModelContext modelContext = inputParam(
                context.getGroupName(),
                context.resolvedMethodParameter().getParameterType(),
                context.getDocumentationType(),
                context.getAlternateTypeProvider(),
                context.getGenericNamingStrategy(),
                context.getIgnorableParameterTypes());
        return ResolvedTypes.modelRefFactory(modelContext, nameExtractor);
    }

}
