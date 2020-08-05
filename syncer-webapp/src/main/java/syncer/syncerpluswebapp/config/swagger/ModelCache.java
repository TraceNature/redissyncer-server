package syncer.syncerpluswebapp.config.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Function;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import springfox.documentation.schema.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.TypeNameProviderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import syncer.syncerpluswebapp.config.swagger.model.ApiJsonObject;
import syncer.syncerpluswebapp.config.swagger.model.ApiJsonProperty;
import syncer.syncerpluswebapp.config.swagger.model.ApiJsonResult;
import syncer.syncerpluswebapp.config.swagger.model.ApiSingleParam;

import java.lang.reflect.Field;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.spi.schema.contexts.ModelContext.inputParam;

/**
 * Created by yueh on 2018/9/13.
 */
public class ModelCache {

    private Map<String, Model> knownModels = new HashMap<>();
    private DocumentationContext context;
    private Function<ResolvedType, ? extends ModelReference> factory;
    private TypeResolver typeResolver = new TypeResolver();
    private Map<String, ApiSingleParam> paramMap = new HashMap<>();
    private Class<?> cls;


    private ModelCache() {
    }


    public static ModelCache getInstance() {
        return ModelCacheSub.instance;
    }

    public ModelCache setParamMap(Map<String, ApiSingleParam> paramMap) {
        this.paramMap = paramMap;
        return getInstance();
    }

    public ModelCache setParamClass(Class<?> cls) {
        this.cls = cls;
        return getInstance();
    }

    public Class<?> getParamClass() {
        return cls;
    }


    public ModelCache setFactory(Function<ResolvedType, ? extends ModelReference> factory) {
        this.factory = factory;
        return getInstance();
    }

    public void setContext(DocumentationContext context) {
        this.context = context;
    }

    public DocumentationContext getContext() {
        return context;
    }

    public Map<String, Model> getKnownModels() {
        return knownModels;
    }


    public ModelCache addModel(ApiJsonObject jsonObj) {
        String modelName = jsonObj.name();

        knownModels.put(modelName,
                new Model(modelName,
                        modelName,
                        new TypeResolver().resolve(String.class),
                        "syncer.syncerpluswebapp.config.swagger.CommonData",
                        toPropertyMap(jsonObj.value()),
                        "POST参数",
                        "",
                        "",
                        newArrayList(), null, null
                ));
        String resultName = jsonObj.name() + "-" + "result";

        knownModels.put(resultName,
                new Model(resultName,
                        resultName,
                        new TypeResolver().resolve(String.class),
                        "syncer.syncerpluswebapp.config.swagger.CommonData",
                        toResultMap(jsonObj.result(), resultName),
                        "返回模型",
                        "",
                        "",
                        newArrayList(), null, null
                ));
        return ModelCacheSub.instance;
    }

    public Map<String, ModelProperty> toResultMap(ApiJsonResult jsonResult, String groupName) {
//        System.out.println("--- toResultMap ---");
        List<String> values = Arrays.asList(jsonResult.value());
        List<String> outer = new ArrayList<>();

        if (!CommonData.getResultTypeOther().equals(jsonResult.type())) {
//            outer.add(CommonData.getJsonErrorCode());
//            outer.add(CommonData.getJsonErrorMsg());
            if (!CommonData.getResultTypeNormal().equals(jsonResult.type())) {
                //model
                String subModelName = groupName + "-" + jsonResult.name();
                knownModels.put(subModelName,
                        new Model(subModelName,
                                subModelName,
                                new TypeResolver().resolve(String.class),
                                "syncer.syncerpluswebapp.config.swagger.CommonData",
                                transResultMap(values),
                                "返回模型",
                                "",
                                "",
                                newArrayList(), null, null
                        ));

                //prop
                Map<String, ModelProperty> propertyMap = new HashMap<>();

//                outer.add(jsonResult.name());
                ResolvedType type = new TypeResolver().resolve(List.class);
                ModelProperty mp = new ModelProperty(
                        jsonResult.name(),
                        type,
                        "",
                        0,
                        false,
                        false,
                        true,
                        false,
                        "",
                        null,
                        "",
                        null,
                        "",
                        null,
                        newArrayList()
                );// new AllowableRangeValues("1", "2000"),//.allowableValues(new AllowableListValues(["ABC", "ONE", "TWO"], "string"))
                mp.updateModelRef(getModelRef());
                ResolvedType collectionElementType = collectionElementType(type);
                try {
                    Field f = ModelProperty.class.getDeclaredField("modelRef");
                    f.setAccessible(true);
                    f.set(mp, new ModelRef("List", new ModelRef(subModelName)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                propertyMap.put(jsonResult.name(), mp);

                if (CommonData.getResultTypePage().equals(jsonResult.type())) {
                    outer.add(CommonData.getJsonStartPageNum());
                    outer.add(CommonData.getJsonPageSize());
                    outer.add(CommonData.getJsonTotalCount());
                }


                propertyMap.putAll(transResultMap(outer));
                return propertyMap;
            }

            outer.addAll(values);
            return transResultMap(outer);
        }

        return transResultMap(values);
    }

    public Map<String, ModelProperty> transResultMap(List<String> values) {
        Map<String, ModelProperty> propertyMap = new HashMap<>();
        for (String resultName : values) {
            ApiSingleParam param = paramMap.get(resultName);
            if (isEmpty(param)) {
                continue;
            }
            Class<?> type = param.type();
            if (!isEmpty(param)) {
                type = param.type();
            } else if (isEmpty(type)) {
                type = String.class;
            }

            boolean allowMultiple = param.allowMultiple();
            if (!isEmpty(param)) {
                allowMultiple = param.allowMultiple();
            }
            ResolvedType resolvedType = null;
            if (allowMultiple) {
                resolvedType = new TypeResolver().resolve(List.class, type);
            } else {
                resolvedType = new TypeResolver().resolve(type);
            }
            ModelProperty mp = new ModelProperty(
                    resultName,
                    resolvedType,
                    param.type().getName(),
                    0,
                    false,
                    false,
                    true,
                    false,
                    param.value(),
                    null,
                    param.example(),
                    null,
                    "",
                    null,
                    newArrayList()
            );// new AllowableRangeValues("1", "2000"),//.allowableValues(new AllowableListValues(["ABC", "ONE", "TWO"], "string"))
            mp.updateModelRef(getModelRef());
            propertyMap.put(resultName, mp);
        }

        return propertyMap;
    }

    public Map<String, ModelProperty> toPropertyMap(ApiJsonProperty[] jsonProp) {
//        System.out.println("--- toPropertyMap ---");
        Map<String, ModelProperty> propertyMap = new HashMap<String, ModelProperty>();

        for (ApiJsonProperty property : jsonProp) {
            String propertyName = property.name();
            ApiSingleParam param = paramMap.get(propertyName);

            String description = property.description();
            if (isNullOrEmpty(description) && !isEmpty(param)) {
                description = param.value();
            }
            String example = property.description();
            if (isNullOrEmpty(example) && !isEmpty(param)) {
                example = param.example();
            }
            Class<?> type = property.type();
            if (!isEmpty(param)) {
                type = param.type();
            } else if (isEmpty(type)) {
                type = String.class;
            }

            boolean allowMultiple = property.allowMultiple();
            if (!isEmpty(param)) {
                allowMultiple = param.allowMultiple();
            }
            ResolvedType resolvedType = null;
            if (allowMultiple) {
                resolvedType = new TypeResolver().resolve(List.class, type);
            } else {
                resolvedType = new TypeResolver().resolve(type);
            }
//            System.out.println("----- example: " + example);
//            System.out.println("----- description: " + description);
            ModelProperty mp = new ModelProperty(
                    propertyName,
                    resolvedType,
                    type.toString(),
                    0,
                    property.required(),
                    false,
                    property.readOnly(),
                    null,
                    description,
                    null,
                    example,
                    null,
                    property.defaultValue(),
                    null,
                    newArrayList()
            );// new AllowableRangeValues("1", "2000"),//.allowableValues(new AllowableListValues(["ABC", "ONE", "TWO"], "string"))
            mp.updateModelRef(getModelRef());
            propertyMap.put(property.name(), mp);
        }

        return propertyMap;
    }


    private static class ModelCacheSub {
        private static ModelCache instance = new ModelCache();
    }

    private Function<ResolvedType, ? extends ModelReference> getModelRef() {
        Function<ResolvedType, ? extends ModelReference> factory = getFactory();
//        ModelReference stringModel = factory.apply(typeResolver.resolve(List.class, String.class));
        return factory;

    }


    public Function<ResolvedType, ? extends ModelReference> getFactory() {
        if (factory == null) {

            List<DefaultTypeNameProvider> providers = newArrayList();
            providers.add(new DefaultTypeNameProvider());
            PluginRegistry<TypeNameProviderPlugin, DocumentationType> modelNameRegistry =
                    OrderAwarePluginRegistry.create(providers);
            TypeNameExtractor typeNameExtractor = new TypeNameExtractor(
                    typeResolver,
                    modelNameRegistry,
                    new JacksonEnumTypeDeterminer());
            ModelContext modelContext = inputParam(
                    context.getGroupName(),
                    String.class,
                    context.getDocumentationType(),
                    context.getAlternateTypeProvider(),
                    context.getGenericsNamingStrategy(),
                    context.getIgnorableParameterTypes());
            factory = ResolvedTypes.modelRefFactory(modelContext, typeNameExtractor);
        }
        return factory;
    }
}
