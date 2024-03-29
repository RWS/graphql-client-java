package com.sdl.web.pca.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Strings;
import com.sdl.web.pca.client.contentmodel.ContextData;
import com.sdl.web.pca.client.contentmodel.Pagination;
import com.sdl.web.pca.client.contentmodel.enums.ContentIncludeMode;
import com.sdl.web.pca.client.contentmodel.enums.ContentNamespace;
import com.sdl.web.pca.client.contentmodel.enums.ContentType;
import com.sdl.web.pca.client.contentmodel.enums.DataModelType;
import com.sdl.web.pca.client.contentmodel.enums.DcpType;
import com.sdl.web.pca.client.contentmodel.enums.ModelServiceLinkRendering;
import com.sdl.web.pca.client.contentmodel.enums.PageInclusion;
import com.sdl.web.pca.client.contentmodel.enums.TcdlLinkRendering;
import com.sdl.web.pca.client.contentmodel.generated.Ancestor;
import com.sdl.web.pca.client.contentmodel.generated.BinaryComponent;
import com.sdl.web.pca.client.contentmodel.generated.ComponentPresentation;
import com.sdl.web.pca.client.contentmodel.generated.ComponentPresentationConnection;
import com.sdl.web.pca.client.contentmodel.generated.ContentComponent;
import com.sdl.web.pca.client.contentmodel.generated.InputComponentPresentationFilter;
import com.sdl.web.pca.client.contentmodel.generated.InputItemFilter;
import com.sdl.web.pca.client.contentmodel.generated.InputPublicationFilter;
import com.sdl.web.pca.client.contentmodel.generated.InputSortParam;
import com.sdl.web.pca.client.contentmodel.generated.Item;
import com.sdl.web.pca.client.contentmodel.generated.ItemConnection;
import com.sdl.web.pca.client.contentmodel.generated.Page;
import com.sdl.web.pca.client.contentmodel.generated.PageConnection;
import com.sdl.web.pca.client.contentmodel.generated.Publication;
import com.sdl.web.pca.client.contentmodel.generated.PublicationConnection;
import com.sdl.web.pca.client.contentmodel.generated.PublicationMapping;
import com.sdl.web.pca.client.contentmodel.generated.SitemapItem;
import com.sdl.web.pca.client.contentmodel.generated.TaxonomySitemapItem;
import com.sdl.web.pca.client.exception.ApiClientException;
import com.sdl.web.pca.client.exception.GraphQLClientException;
import com.sdl.web.pca.client.exception.UnauthorizedException;
import com.sdl.web.pca.client.jsonmapper.ContentComponentDeserializer;
import com.sdl.web.pca.client.jsonmapper.ItemDeserializer;
import com.sdl.web.pca.client.jsonmapper.SitemapDeserializer;
import com.sdl.web.pca.client.query.PCARequestBuilder;
import com.sdl.web.pca.client.request.GraphQLRequest;
import com.sdl.web.pca.client.util.CmUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sdl.web.pca.client.modelserviceplugin.ClaimHelper.createClaim;
import static com.sdl.web.pca.client.modelserviceplugin.ClaimHelper.createClaimTcdlBinaryLinkUrlPrefix;
import static com.sdl.web.pca.client.modelserviceplugin.ClaimHelper.createClaimTcdlLinkUrlPrefix;

public class DefaultApiClient implements ApiClient, GraphQLClient {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApiClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GraphQLClient client;
    private int requestTimeout;

    private ContextData globalContextData = new ContextData();
    private ContentType defaultContentType = ContentType.MODEL;
    private DataModelType defaultModelType = DataModelType.R2;
    private TcdlLinkRendering tcdlLinkRenderingType = TcdlLinkRendering.RELATIVE;
    private ModelServiceLinkRendering modelServiceLinkRenderingType = ModelServiceLinkRendering.RELATIVE;
    private String tcdlLinkUrlPrefix = null;
    private String tcdlBinaryLinkUrlPrefix = null;

    static {
        SimpleModule module = new SimpleModule() {
            public Object getTypeId() {
                return DefaultApiClient.class.getName();
            }
        };
        module.addDeserializer(SitemapItem.class, new SitemapDeserializer(SitemapItem.class, MAPPER));
        module.addDeserializer(ContentComponent.class, new ContentComponentDeserializer(ContentComponent.class, MAPPER));
        module.addDeserializer(Item.class, new ItemDeserializer(Item.class, MAPPER));
        MAPPER.registerModule(module);
        LOG.info("Custom deserializers are loaded");
    }

    public DefaultApiClient(GraphQLClient graphQLClient) {
        this(graphQLClient, 0);
    }

    public DefaultApiClient(GraphQLClient graphQLClient, int requestTimeout) {
        this.client = graphQLClient;
        this.requestTimeout = (int) TimeUnit.MILLISECONDS.toMillis(requestTimeout);
    }

    @Override
    public ContextData getGlobalContextData() {
        return globalContextData;
    }

    @Override
    public void setGlobalContextData(ContextData globalContextData) {
        this.globalContextData = globalContextData;
    }

    @Override
    public ContentType getDefaultContentType() {
        return defaultContentType;
    }

    @Override
    public void setDefaultContentType(ContentType contentType) {
        this.defaultContentType = contentType;
    }

    @Override
    public DataModelType getDefaultModelType() {
        return defaultModelType;
    }

    @Override
    public void setDefaultModelType(DataModelType dataModelType) {
        this.defaultModelType = dataModelType;
    }

    @Override
    public TcdlLinkRendering getTcdlLinkRenderingType() {
        return tcdlLinkRenderingType;
    }

    @Override
    public void setTcdlLinkRenderingType(TcdlLinkRendering tcdlLinkRenderingType) {
        this.tcdlLinkRenderingType = tcdlLinkRenderingType;
    }

    @Override
    public ModelServiceLinkRendering getModelServiceLinkRenderingType() {
        return modelServiceLinkRenderingType;
    }

    @Override
    public void setModelServiceLinkRenderingType(ModelServiceLinkRendering renderingType) {
        this.modelServiceLinkRenderingType = renderingType;
    }

    @Override
    public String getTcdlLinkUrlPrefix() {
        return tcdlLinkUrlPrefix;
    }

    @Override
    public void setTcdlLinkUrlPrefix(String tcdlLinkUrlPrefix) {
        this.tcdlLinkUrlPrefix = tcdlLinkUrlPrefix;
    }

    @Override
    public String getTcdlBinaryLinkUrlPrefix() {
        return tcdlBinaryLinkUrlPrefix;
    }

    @Override
    public void setTcdlBinaryLinkUrlPrefix(String tcdlBinaryLinkUrlPrefix) {
        this.tcdlBinaryLinkUrlPrefix = tcdlBinaryLinkUrlPrefix;
    }

    @Override
    public void addDefaultHeader(String header, String value) {
        this.client.addDefaultHeader(header, value);
    }

    @Override
    public String execute(String jsonEntity, int timeout) throws UnauthorizedException, GraphQLClientException {
        return client.execute(jsonEntity, timeout);
    }

    @Override
    public String execute(String jsonEntity) throws UnauthorizedException, GraphQLClientException {
        return client.execute(jsonEntity);
    }

    @Override
    public String execute(GraphQLRequest request) throws UnauthorizedException, GraphQLClientException {
        return client.execute(request);
    }

    @Override
    public ComponentPresentation getComponentPresentation(ContentNamespace ns, int publicationId, int componentId,
                                                          int templateId, String customMetaFilter,
                                                          ContentIncludeMode contentIncludeMode, ContextData contextData) {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ComponentPresentation")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("componentId", componentId)
                .withVariable("templateId", templateId)
                .withCustomMetaFilter(customMetaFilter)
                .withContentIncludeMode(contentIncludeMode)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();
        return getResultForRequest(graphQLRequest, ComponentPresentation.class, "/data/componentPresentation");
    }

    @Override
    public ComponentPresentationConnection getComponentPresentations(ContentNamespace ns, int publicationId,
                                                                     InputComponentPresentationFilter filter, InputSortParam sort, Pagination pagination,
                                                                     String customMetaFilter, ContentIncludeMode contentIncludeMode, ContextData contextData) {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ComponentPresentations")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withInputComponentPresentationFilter(filter)
                .withInputSortParam(sort)
                .withPagination(pagination)
                .withCustomMetaFilter(customMetaFilter)
                .withContentIncludeMode(contentIncludeMode)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, ComponentPresentationConnection.class, "/data/componentPresentations");
    }

    @Override
    public Page getPage(ContentNamespace ns, int publicationId, int pageId, String customMetaFilter, ContentIncludeMode contentIncludeMode, ContextData contextData) {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PageById")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("pageId", pageId)
                .withCustomMetaFilter(customMetaFilter)
                .withContentIncludeMode(contentIncludeMode)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();
        return getResultForRequest(graphQLRequest, Page.class, "/data/page");
    }

    @Override
    public Page getPage(ContentNamespace ns, int publicationId, String url, String customMetaFilter, ContentIncludeMode contentIncludeMode, ContextData contextData) {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PageByUrl")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("url", url)
                .withCustomMetaFilter(customMetaFilter)
                .withContentIncludeMode(contentIncludeMode)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, Page.class, "/data/page");
    }

    @Override
    public Page getPage(CmUri cmUri, String customMetaFilter, ContentIncludeMode contentIncludeMode, ContextData contextData) {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PageByCmUri")
                .withCmUri(cmUri)
                .withCustomMetaFilter(customMetaFilter)
                .withContentIncludeMode(contentIncludeMode)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, Page.class, "/data/page");
    }

    @Override
    public PageConnection getPages(ContentNamespace ns, Pagination pagination, String url, String customMetaFilter, ContentIncludeMode contentIncludeMode, ContextData contextData) {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PagesByUrl")
                .withNamespace(ns)
                .withPagination(pagination)
                .withVariable("url", url)
                .withCustomMetaFilter(customMetaFilter)
                .withContentIncludeMode(contentIncludeMode)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, PageConnection.class, "/data/pages");
    }

    @Override
    public BinaryComponent getBinaryComponent(ContentNamespace ns, int publicationId, int binaryId, String customMetaFilter,
                                              ContextData contextData) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("BinaryComponentById")
                .withCustomMetaFilter(customMetaFilter)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("binaryId", binaryId)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, BinaryComponent.class, "/data/binaryComponent");
    }

    @Override
    public BinaryComponent getBinaryComponent(ContentNamespace ns, int publicationId, String url, String customMetaFilter,
                                              ContextData contextData) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("BinaryComponentByUrl")
                .withVariantArgs(url)
                .withCustomMetaFilter(customMetaFilter)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("url", url)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, BinaryComponent.class, "/data/binaryComponent");
    }

    @Override
    public BinaryComponent getBinaryComponent(CmUri cmUri, String customMetaFilter, ContextData contextData)
            throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("BinaryComponentByCmUri")
                .withCustomMetaFilter(customMetaFilter)
                .withVariable("namespaceId", cmUri.getNamespaceId())
                .withVariable("publicationId", cmUri.getPublicationId())
                .withVariable("cmUri", cmUri.toString())
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, BinaryComponent.class, "/data/binaryComponent");
    }

    @Override
    public ItemConnection executeItemQuery(InputItemFilter filter, InputSortParam sort, Pagination pagination,
                                           String customMetaFilter, ContentIncludeMode contentIncludeMode,
                                           boolean includeContainerItems, ContextData contextData) throws ApiClientException {

        // We only include the fragments that will be required based on the item types in the
        // input item filter
        List<String> fragments = new ArrayList<>();
        if (filter != null && filter.getItemTypes() != null) {
            fragments = mapToFragmentList(filter);
        }

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ItemQuery")
                .withInjectFragments(fragments)
                .withIncludeRegion("includeContainerItems", includeContainerItems)
                .withContentIncludeMode(contentIncludeMode)
                .withCustomMetaFilter(customMetaFilter)
                .withVariable("first", pagination.getFirst())
                .withVariable("after", pagination.getAfter())
                .withVariable("inputItemFilter", filter)
                .withVariable("inputSortParam", sort)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, ItemConnection.class, "/data/items");
    }

    List<String> mapToFragmentList(InputItemFilter filter) {
        return filter.getItemTypes().stream().map(type -> Arrays.stream(type.toString().split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .reduce("", String::concat)
                + "Fields"
        ).collect(Collectors.toList());
    }

    @Override
    public Publication getPublication(ContentNamespace ns, int publicationId, String customMetaFilter,
                                      ContextData contextData) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("Publication")
                .withCustomMetaFilter(customMetaFilter)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, Publication.class, "/data/publication");
    }

    @Override
    public PublicationConnection getPublications(ContentNamespace ns, Pagination pagination, InputPublicationFilter filter,
                                                 String customMetaFilter,
                                                 ContextData contextData) {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("Publications")
                .withCustomMetaFilter(customMetaFilter)
                .withNamespace(ns)
                .withVariable("first", pagination.getFirst())
                .withVariable("after", pagination.getAfter())
                .withVariable("filter", filter)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, PublicationConnection.class, "/data/publications");
    }

    @Override
    public String resolvePageLink(ContentNamespace ns, int publicationId, int pageId,
                                  boolean renderRelativeLink) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ResolvePageLink")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("pageId", pageId)
                .withVariable("renderRelativeLink", renderRelativeLink)
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/pageLink/url").asText();
    }

    @Override
    public String resolveComponentLink(ContentNamespace ns, int publicationId, int componentId, Integer sourcePageId,
                                       Integer excludeComponentTemplateId,
                                       boolean renderRelativeLink) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ResolveComponentLink")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("targetComponentId", componentId)
                .withVariable("sourcePageId", sourcePageId)
                .withVariable("excludeComponentTemplateId", excludeComponentTemplateId)
                .withVariable("renderRelativeLink", renderRelativeLink)
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/componentLink/url").asText();
    }

    @Override
    public String resolveBinaryLink(ContentNamespace ns, int publicationId, int binaryId,
                                    String variantId, boolean renderRelativeLink) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ResolveBinaryLink")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("binaryId", binaryId)
                .withVariable("variantId", variantId)
                .withVariable("renderRelativeLink", renderRelativeLink)
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/binaryLink/url").asText();
    }

    @Override
    public String resolveDynamicComponentLink(ContentNamespace ns, int publicationId, int pageId, int componentId,
                                              int templateId,
                                              boolean renderRelativeLink) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("ResolveDynamicComponentLink")
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("targetPageId", pageId)
                .withVariable("targetComponentId", componentId)
                .withVariable("targetTemplateId", templateId)
                .withVariable("renderRelativeLink", renderRelativeLink)
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/dynamicComponentLink/url").asText();
    }

    @Override
    public PublicationMapping getPublicationMapping(ContentNamespace ns, String url) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PublicationMapping")
                .withNamespace(ns)
                .withVariable("siteUrl", url)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, PublicationMapping.class, "/data/publicationMapping");
    }

    @Override
    public JsonNode getPageModelData(ContentNamespace ns, int publicationId, String url, ContentType contentType,
                                     DataModelType modelType, PageInclusion pageInclusion, ContentIncludeMode contentIncludeMode,
                                     ContextData contextData) throws ApiClientException {
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PageModelByUrl")
                .withContentIncludeMode(contentIncludeMode)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("url", url)
                .withContextData(globalContextDataInternal(), contextData)
                .withClaim(createClaim(contentType))
                .withClaim(createClaim(modelType))
                .withClaim(createClaim(pageInclusion))
                .withOperation("page")
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/page/rawContent/data");
    }

    @Override
    public JsonNode getPageModelData(ContentNamespace ns, int publicationId, int pageId, ContentType contentType,
                                     DataModelType modelType, PageInclusion pageInclusion, ContentIncludeMode contentIncludeMode,
                                     ContextData contextData) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("PageModelById")
                .withContentIncludeMode(contentIncludeMode)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("pageId", pageId)
                .withContextData(globalContextDataInternal(), contextData)
                .withClaim(createClaim(contentType))
                .withClaim(createClaim(modelType))
                .withClaim(createClaim(pageInclusion))
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/page/rawContent/data");
    }

    @Override
    public JsonNode getEntityModelData(ContentNamespace ns, int publicationId, int entityId, int templateId,
                                       ContentType contentType,
                                       DataModelType modelType, DcpType dcpType, ContentIncludeMode contentIncludeMode,
                                       ContextData contextData) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("EntityModelById")
                .withContentIncludeMode(contentIncludeMode)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("componentId", entityId)
                .withVariable("templateId", templateId)
                .withContextData(globalContextDataInternal(), contextData)
                .withClaim(createClaim(contentType))
                .withClaim(createClaim(modelType))
                .withClaim(createClaim(dcpType))
                .withTimeout(requestTimeout)
                .build();

        return getJsonResult(graphQLRequest, "/data/componentPresentation/rawContent/data");
    }

    @Override
    public TaxonomySitemapItem getSitemap(ContentNamespace ns, int publicationId, int descendantLevels,
                                          ContextData contextData) throws ApiClientException {

        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery("Sitemap")
                .withRecurseFragment("RecurseItems", descendantLevels)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, TaxonomySitemapItem.class, "/data/sitemap");
    }

    @Override
    public TaxonomySitemapItem[] getSitemapSubtree(ContentNamespace ns, int publicationId, String taxonomyNodeId,
                                                   int descendantLevels, Ancestor ancestor,
                                                   ContextData contextData) throws ApiClientException {

        String query = "SitemapSubtree";
        if (descendantLevels == 0) {
            query = "SitemapSubtreeNoRecurse";
        }
        GraphQLRequest graphQLRequest = new PCARequestBuilder()
                .withQuery(query)
                .withRecurseFragment("RecurseItems", descendantLevels)
                .withNamespace(ns)
                .withPublicationId(publicationId)
                .withVariable("taxonomyNodeId", taxonomyNodeId)
                .withVariable("ancestor", ancestor)
                .withContextData(globalContextDataInternal(), contextData)
                .withTimeout(requestTimeout)
                .build();

        return getResultForRequest(graphQLRequest, TaxonomySitemapItem[].class, "/data/sitemapSubtree");
    }


    private <T> T getResultForRequest(GraphQLRequest request, Class<T> clazz, String path) throws ApiClientException {
        JsonNode result = getJsonResult(request, path);
        try {
            return MAPPER.treeToValue(result, clazz);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("Unable map result to " + clazz.getName() + ": " + result.toString(), e);
        }
    }

    private JsonNode getJsonResult(GraphQLRequest request, String path) throws ApiClientException {
        int attempt = 3;
        UnauthorizedException[] exception = new UnauthorizedException[1];
        while(attempt > 0) {
            try {
                attempt--;
                return getJsonResultInternal(request, path);
            } catch (UnauthorizedException ex) {
                if (exception[0] == null) exception[0] = ex;
                LOG.error("Could not perform query on " + path);
            }
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new ApiClientException("Could not perform query " + request + " after 3 attempts", exception[0]);
    }

    private JsonNode getJsonResultInternal(GraphQLRequest request, String path) throws ApiClientException, UnauthorizedException {
        try {
            String resultString = client.execute(request);
            JsonNode resultJson = MAPPER.readTree(resultString);
            return resultJson.at(path);
        } catch (GraphQLClientException e) {
            throw new ApiClientException("Unable to execute query: " + request, e);
        } catch (IOException e) {
            throw new ApiClientException("Unable to deserialize result for query " + request, e);
        }
    }

    private ContextData globalContextDataInternal() {
        ContextData data = new ContextData();
        data.addClaimValues(globalContextData);
        // Add a default claim here to control model type returned by default
        data.addClaimValue(createClaim(defaultModelType));
        data.addClaimValue(createClaim(defaultContentType));
        // Add claim to control how tcdl links are rendered
        data.addClaimValue(createClaim(tcdlLinkRenderingType));
        // Add claim to control how model-service plugin renders links
        data.addClaimValue(createClaim(modelServiceLinkRenderingType));
        // Add claim to control prefix urls
        if (tcdlLinkRenderingType != TcdlLinkRendering.ABSOLUTE) {
            return data;
        }
        if (!Strings.isNullOrEmpty(tcdlLinkUrlPrefix)) {
            data.addClaimValue(createClaimTcdlLinkUrlPrefix(tcdlLinkUrlPrefix));
        }
        if (!Strings.isNullOrEmpty(tcdlBinaryLinkUrlPrefix)) {
            data.addClaimValue(createClaimTcdlBinaryLinkUrlPrefix(tcdlBinaryLinkUrlPrefix));
        }
        return data;
    }
}
