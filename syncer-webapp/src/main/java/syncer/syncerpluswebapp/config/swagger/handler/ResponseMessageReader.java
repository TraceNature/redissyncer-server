package syncer.syncerpluswebapp.config.swagger.handler;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.Header;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.readers.operation.SwaggerResponseMessageReader;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static springfox.documentation.schema.ResolvedTypes.modelRefFactory;
import static springfox.documentation.spi.schema.contexts.ModelContext.returnValue;
import static springfox.documentation.spring.web.readers.operation.ResponseMessagesReader.httpStatusCode;
import static springfox.documentation.spring.web.readers.operation.ResponseMessagesReader.message;
import static springfox.documentation.swagger.annotations.Annotations.resolvedTypeFromOperation;
import static springfox.documentation.swagger.annotations.Annotations.resolvedTypeFromResponse;
import static springfox.documentation.swagger.readers.operation.ResponseHeaders.headers;
import static springfox.documentation.swagger.readers.operation.ResponseHeaders.responseHeaders;

/**
 * Created by yueh on 2018/9/13.
 */
@Primary
@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 3)
public class ResponseMessageReader extends SwaggerResponseMessageReader {


    private final TypeNameExtractor typeNameExtractor;
    private final TypeResolver typeResolver;

    public ResponseMessageReader(TypeNameExtractor typeNameExtractor, TypeResolver typeResolver) {
        super(typeNameExtractor, typeResolver);
        this.typeNameExtractor = typeNameExtractor;
        this.typeResolver = typeResolver;
    }

    @Override
    protected Set<ResponseMessage> read(OperationContext context) {
        ResolvedType defaultResponse = context.getReturnType();
        Optional<ApiOperation> operationAnnotation = context.findAnnotation(ApiOperation.class);
        Optional<ResolvedType> operationResponse =
                operationAnnotation.transform(resolvedTypeFromOperation(typeResolver, defaultResponse));
        Optional<ResponseHeader[]> defaultResponseHeaders = operationAnnotation.transform(responseHeaders());
        Map<String, Header> defaultHeaders = newHashMap();
        if (defaultResponseHeaders.isPresent()) {
            defaultHeaders.putAll(headers(defaultResponseHeaders.get()));
        }

        List<ApiResponses> allApiResponses = context.findAllAnnotations(ApiResponses.class);
        Set<ResponseMessage> responseMessages = newHashSet();

        Map<Integer, ApiResponse> seenResponsesByCode = newHashMap();
        for (ApiResponses apiResponses : allApiResponses) {
            ApiResponse[] apiResponseAnnotations = apiResponses.value();
            for (ApiResponse apiResponse : apiResponseAnnotations) {

                if (!seenResponsesByCode.containsKey(apiResponse.code())) {
                    seenResponsesByCode.put(apiResponse.code(), apiResponse);

                    java.util.Optional<ModelReference> responseModel = java.util.Optional.empty();
                    java.util.Optional<ResolvedType> type = resolvedType(null, apiResponse);
                    if (isSuccessful(apiResponse.code())) {
                        type = java.util.Optional.ofNullable(type.orElseGet(operationResponse::get));
                    }
                    if (type.isPresent()) {
                        responseModel = java.util.Optional.of(new ModelRef(apiResponse.reference()+"-result"));

                    }
                    Map<String, Header> headers = newHashMap(defaultHeaders);
                    headers.putAll(headers(apiResponse.responseHeaders()));

                    responseMessages.add(new ResponseMessageBuilder()
                            .code(apiResponse.code())
                            .message(apiResponse.message())
                            .responseModel(responseModel.orElse(null))
                            .headersWithDescription(headers)
                            .build());
                }
            }
        }
        if (operationResponse.isPresent()) {
            ModelContext modelContext = returnValue(
                    context.getGroupName(),
                    operationResponse.get(),
                    context.getDocumentationType(),
                    context.getAlternateTypeProvider(),
                    context.getGenericsNamingStrategy(),
                    context.getIgnorableParameterTypes());
            ResolvedType resolvedType = context.alternateFor(operationResponse.get());

            ModelReference responseModel = modelRefFactory(modelContext, typeNameExtractor).apply(resolvedType);
            context.operationBuilder().responseModel(responseModel);
            ResponseMessage defaultMessage = new ResponseMessageBuilder()
                    .code(httpStatusCode(context))
                    .message(message(context))
                    .responseModel(responseModel)
                    .build();
            if (!responseMessages.contains(defaultMessage) && !"void".equals(responseModel.getType())) {
                responseMessages.add(defaultMessage);
            }
        }

        return responseMessages;
    }


    static boolean isSuccessful(int code) {
        try {
            return HttpStatus.Series.SUCCESSFUL.equals(HttpStatus.Series.valueOf(code));
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private java.util.Optional<ResolvedType> resolvedType(
            ResolvedType resolvedType,
            ApiResponse apiResponse) {
        return java.util.Optional.ofNullable(resolvedTypeFromResponse(
                typeResolver,
                resolvedType).apply(apiResponse));
    }
}
