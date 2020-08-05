package syncer.syncerpluswebapp.config.swagger.handler;

/**
 * Created by yueh on 2018/9/12.
 */

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.schema.Model;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.paths.PathMappingAdjuster;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.scanners.ApiDescriptionReader;
import springfox.documentation.spring.web.scanners.ApiListingScanner;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;
import springfox.documentation.spring.web.scanners.ApiModelReader;
import syncer.syncerpluswebapp.config.swagger.ModelCache;

import java.util.*;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static springfox.documentation.builders.BuilderDefaults.nullToEmptyList;
import static springfox.documentation.spi.service.contexts.Orderings.methodComparator;
import static springfox.documentation.spi.service.contexts.Orderings.resourceGroupComparator;

@Component
@Primary
public class ApiListingJsonScanner extends ApiListingScanner {
    private final ApiDescriptionReader apiDescriptionReader;
    private final ApiModelReader apiModelReader;
    private final DocumentationPluginsManager pluginsManager;
    public static final String ROOT = "/";

    @Autowired
    public ApiListingJsonScanner(
            ApiDescriptionReader apiDescriptionReader,
            ApiModelReader apiModelReader,
            DocumentationPluginsManager pluginsManager) {
        super(apiDescriptionReader,apiModelReader,pluginsManager);
        this.apiDescriptionReader = apiDescriptionReader;
        this.apiModelReader = apiModelReader;
        this.pluginsManager = pluginsManager;
    }

    static Optional<String> longestCommonPath(List<ApiDescription> apiDescriptions) {

        List<String> commons = newArrayList();
        if (null == apiDescriptions || apiDescriptions.isEmpty()) {
            return Optional.absent();
        }
        List<String> firstWords = urlParts(apiDescriptions.get(0));

        for (int position = 0; position < firstWords.size(); position++) {
            String word = firstWords.get(position);
            boolean allContain = true;
            for (int i = 1; i < apiDescriptions.size(); i++) {
                List<String> words = urlParts(apiDescriptions.get(i));
                if (words.size() < position + 1 || !words.get(position).equals(word)) {
                    allContain = false;
                    break;
                }
            }
            if (allContain) {
                commons.add(word);
            }
        }

        Joiner joiner = Joiner.on("/").skipNulls();
        return Optional.of("/" + joiner.join(commons));
    }

    static List<String> urlParts(ApiDescription apiDescription) {
        return Splitter.on('/')
                .omitEmptyStrings()
                .trimResults()
                .splitToList(apiDescription.getPath());
    }

    @Override
    public Multimap<String, ApiListing> scan(ApiListingScanningContext context) {
        final Multimap<String, ApiListing> apiListingMap = LinkedListMultimap.create();
        int position = 0;
        Map<ResourceGroup, List<RequestMappingContext>> requestMappingsByResourceGroup
                = context.getRequestMappingsByResourceGroup();
        Collection<ApiDescription> additionalListings = pluginsManager.additionalListings(context);
        Set<ResourceGroup> allResourceGroups = FluentIterable.from(collectResourceGroups(additionalListings))
                .append(requestMappingsByResourceGroup.keySet())
                .toSet();

        List<SecurityReference> securityReferences = newArrayList();
        for (final ResourceGroup resourceGroup : sortedByName(allResourceGroups)) {

            DocumentationContext documentationContext = context.getDocumentationContext();
            Set<String> produces = new LinkedHashSet<String>(documentationContext.getProduces());
            Set<String> consumes = new LinkedHashSet<String>(documentationContext.getConsumes());
            String host = documentationContext.getHost();
            Set<String> protocols = new LinkedHashSet<String>(documentationContext.getProtocols());
            Set<ApiDescription> apiDescriptions = newHashSet();

            Map<String, Model> models = new LinkedHashMap<String, Model>();
            List<RequestMappingContext> requestMappings = nullToEmptyList(requestMappingsByResourceGroup.get(resourceGroup));

            for (RequestMappingContext each : sortedByMethods(requestMappings)) {//url
                Map<String, Model> knownModels = new HashMap<>();
                models.putAll(apiModelReader.read(each.withKnownModels(models)));

                apiDescriptions.addAll(apiDescriptionReader.read(each));
            }
            //
            models.putAll(ModelCache.getInstance().getKnownModels());


            List<ApiDescription> additional = from(additionalListings)
                    .filter(and(
                                    belongsTo(resourceGroup.getGroupName()),
                                    onlySelectedApis(documentationContext)))
                    .toList();
            apiDescriptions.addAll(additional);

            List<ApiDescription> sortedApis = FluentIterable.from(apiDescriptions)
                    .toSortedList(documentationContext.getApiDescriptionOrdering());
            Optional<String> o = longestCommonPath(sortedApis);
            String resourcePath = new ResourcePathProvider(resourceGroup)
                    .resourcePath()
                    .or(o)
                    .orNull();

            PathProvider pathProvider = documentationContext.getPathProvider();
            String basePath = pathProvider.getApplicationBasePath();
            PathAdjuster adjuster = new PathMappingAdjuster(documentationContext);
            ApiListingBuilder apiListingBuilder = new ApiListingBuilder(context.apiDescriptionOrdering())
                    .apiVersion(documentationContext.getApiInfo().getVersion())
                    .basePath(adjuster.adjustedPath(basePath))
                    .resourcePath(resourcePath)
                    .produces(produces)
                    .consumes(consumes)
                    .host(host)
                    .protocols(protocols)
                    .securityReferences(securityReferences)
                    .apis(sortedApis)
                    .models(models)
                    .position(position++)
                    .availableTags(documentationContext.getTags());

            ApiListingContext apiListingContext = new ApiListingContext(
                    context.getDocumentationType(),
                    resourceGroup,
                    apiListingBuilder);
            apiListingMap.put(resourceGroup.getGroupName(), pluginsManager.apiListing(apiListingContext));
        }
        return apiListingMap;
    }
    private Predicate<ApiDescription> onlySelectedApis(final DocumentationContext context) {
        return new Predicate<ApiDescription>() {
            @Override
            public boolean apply(ApiDescription input) {
                return context.getApiSelector().getPathSelector().apply(input.getPath());
            }
        };
    }

    private Iterable<RequestMappingContext> sortedByMethods(List<RequestMappingContext> contexts) {
        return contexts.stream().sorted(methodComparator()).collect(toList());
    }


    static Iterable<ResourceGroup> collectResourceGroups(Collection<ApiDescription> apiDescriptions) {
        return from(apiDescriptions)
                .transform(toResourceGroups());
    }

    static Iterable<ResourceGroup> sortedByName(Set<ResourceGroup> resourceGroups) {
        return from(resourceGroups).toSortedList(resourceGroupComparator());
    }

    static Predicate<ApiDescription> belongsTo(final String groupName) {
        return new Predicate<ApiDescription>() {
            @Override
            public boolean apply(ApiDescription input) {
                return !input.getGroupName().isPresent()
                        || groupName.equals(input.getGroupName().get());
            }
        };
    }

    private static Function<ApiDescription, ResourceGroup> toResourceGroups() {
        return new Function<ApiDescription, ResourceGroup>() {
            @Override
            public ResourceGroup apply(ApiDescription input) {
                return new ResourceGroup(
                        input.getGroupName().or(Docket.DEFAULT_GROUP_NAME),
                        null);
            }
        };
    }

    class ResourcePathProvider {
        private final ResourceGroup resourceGroup;

        ResourcePathProvider(ResourceGroup resourceGroup) {
            this.resourceGroup = resourceGroup;
        }

        public Optional<String> resourcePath() {
            return Optional.fromNullable(
                    Strings.emptyToNull(controllerClass()
                            .transform(resourcePathExtractor())
                            .or("")));
        }

        private Function<Class<?>, String> resourcePathExtractor() {
            return new Function<Class<?>, String>() {
                @Override
                public String apply(Class<?> input) {
                    String path = Iterables.getFirst(Arrays.asList(paths(input)), "");
                    if (Strings.isNullOrEmpty(path)) {
                        return "";
                    }
                    if (path.startsWith("/")) {
                        return path;
                    }
                    return "/" + path;
                }
            };
        }

        @VisibleForTesting
        String[] paths(Class<?> controller) {
            RequestMapping annotation
                    = AnnotationUtils.findAnnotation(controller, RequestMapping.class);
            if (annotation != null) {
                return annotation.path();
            }
            return new String[]{};
        }

        private Optional<? extends Class<?>> controllerClass() {
            return resourceGroup.getControllerClass();
        }
    }
}
