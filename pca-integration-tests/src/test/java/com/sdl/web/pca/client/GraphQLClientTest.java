package com.sdl.web.pca.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.sdl.web.pca.client.contentmodel.ContextData;
import com.sdl.web.pca.client.contentmodel.Pagination;
import com.sdl.web.pca.client.contentmodel.enums.ContentIncludeMode;
import com.sdl.web.pca.client.contentmodel.enums.ContentNamespace;
import com.sdl.web.pca.client.contentmodel.enums.ContentType;
import com.sdl.web.pca.client.contentmodel.enums.DataModelType;
import com.sdl.web.pca.client.contentmodel.enums.DcpType;
import com.sdl.web.pca.client.contentmodel.enums.PageInclusion;
import com.sdl.web.pca.client.contentmodel.generated.Ancestor;
import com.sdl.web.pca.client.contentmodel.generated.BinaryComponent;
import com.sdl.web.pca.client.contentmodel.generated.Component;
import com.sdl.web.pca.client.contentmodel.generated.ComponentPresentation;
import com.sdl.web.pca.client.contentmodel.generated.ComponentPresentationConnection;
import com.sdl.web.pca.client.contentmodel.generated.FilterItemType;
import com.sdl.web.pca.client.contentmodel.generated.InputComponentPresentationFilter;
import com.sdl.web.pca.client.contentmodel.generated.InputItemFilter;
import com.sdl.web.pca.client.contentmodel.generated.ItemConnection;
import com.sdl.web.pca.client.contentmodel.generated.Keyword;
import com.sdl.web.pca.client.contentmodel.generated.Page;
import com.sdl.web.pca.client.contentmodel.generated.PageConnection;
import com.sdl.web.pca.client.contentmodel.generated.Publication;
import com.sdl.web.pca.client.contentmodel.generated.PublicationConnection;
import com.sdl.web.pca.client.contentmodel.generated.PublicationMapping;
import com.sdl.web.pca.client.contentmodel.generated.TaxonomySitemapItem;
import com.sdl.web.pca.client.exception.GraphQLClientException;
import com.sdl.web.pca.client.exception.UnauthorizedException;
import com.sdl.web.pca.client.request.GraphQLRequest;
import com.sdl.web.pca.client.util.CmUri;
import com.sdl.web.pca.client.util.ItemTypes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GraphQLClientTest {

    private DefaultGraphQLClient client = null;
    private Properties prop = null;
    private DefaultApiClient publicContentApi;

    @Before
    public void before() throws Exception {
        prop = new Properties();
        InputStream inputStream = GraphQLClientTest.class.getClassLoader()
                .getResourceAsStream("testconfig.properties");

        prop.load(inputStream);
        client = new DefaultGraphQLClient(prop.getProperty("GRAPHQL_SERVER_ENDPOINT"), null);

        publicContentApi = new DefaultApiClient(client);
    }

    @Ignore("To be fixed")
    @Test
    public void getComponentPresentation() {
        ComponentPresentation result = publicContentApi.getComponentPresentation(ContentNamespace.Sites, 8, 1458,
                9195, "", ContentIncludeMode.EXCLUDE, null);
    }

    @Ignore("To be fixed")
    @Test
    public void getComponentPresentations() {
        ComponentPresentationConnection result = publicContentApi.getComponentPresentations(ContentNamespace.Sites, 8,
                new InputComponentPresentationFilter(), null, null, "", ContentIncludeMode.EXCLUDE, new ContextData());
    }

    @Test
    public void getPageById() {
        Page result = publicContentApi.getPage(ContentNamespace.Sites, 8, 640,
                "", ContentIncludeMode.INCLUDE_DATA, new ContextData());

        assertEquals(640, result.getItemId());
        assertEquals(64, result.getItemType());
        assertEquals("000 Home", result.getTitle());
        assertEquals("/index.html", result.getUrl());
    }

    @Test
    public void getPageByUrl() {
        Page result = publicContentApi.getPage(ContentNamespace.Sites, 8, "/index.html",
                "", ContentIncludeMode.INCLUDE_DATA, new ContextData());

        assertEquals(640, result.getItemId());
        assertEquals(64, result.getItemType());
        assertEquals("000 Home", result.getTitle());
        assertEquals("/index.html", result.getUrl());
    }

    @Test
    public void getPageByCmUri() {
        Page result = publicContentApi.getPage(new CmUri("tcm:8-640-64"),
                "", ContentIncludeMode.INCLUDE_DATA, new ContextData());

        assertEquals(640, result.getItemId());
        assertEquals(64, result.getItemType());
        assertEquals("000 Home", result.getTitle());
        assertEquals("/index.html", result.getUrl());
    }

    @Test
    public void getPages() {
        PageConnection result = publicContentApi.getPages(ContentNamespace.Sites, new Pagination(), "/index.html", "",
                ContentIncludeMode.EXCLUDE, null);

        assertEquals(2, result.getEdges().size());
        assertEquals(640, result.getEdges().get(0).getNode().getItemId());
        assertEquals(1677, result.getEdges().get(1).getNode().getItemId());
    }

    @Test
    public void getBinaryComponentById() throws Exception {
        BinaryComponent result = publicContentApi.getBinaryComponent(ContentNamespace.Sites, 8, 756,
                null, null);

        assertEquals(BinaryComponent.class, result.getClass());
        assertEquals("b4e5c7c4-f04a-3d6d-898f-5886d0f648bd", result.getId());
        assertEquals(1, result.getNamespaceId());
        assertEquals(8, result.getPublicationId());
        assertEquals("tcd:pub[8]/componentmeta[756]", result.getTitle());
        assertEquals(756, result.getVariants().getEdges().get(0).getNode().getBinaryId());
        assertEquals("/media/balloons_tcm8-756.jpg", result.getVariants().getEdges().get(0).getNode().getPath());
        assertEquals("/media/balloons_tcm8-756.jpg", result.getVariants().getEdges().get(0).getNode().getUrl());
        assertEquals("http://localhost:8081/udp/content/binary/1/8/756", result.getVariants().getEdges().get(0).getNode().getDownloadUrl());
        assertEquals("[#def#]", result.getVariants().getEdges().get(0).getNode().getVariantId());

    }

    @Test
    public void getBinaryComponentByUrl() throws Exception {
        BinaryComponent result = publicContentApi.getBinaryComponent(ContentNamespace.Sites, 8,
                "/media/balloons_tcm8-756.jpg", null, null);

        assertEquals(BinaryComponent.class, result.getClass());
        assertEquals("b4e5c7c4-f04a-3d6d-898f-5886d0f648bd", result.getId());
        assertEquals(1, result.getNamespaceId());
        assertEquals(8, result.getPublicationId());
        assertEquals("tcd:pub[8]/componentmeta[756]", result.getTitle());
        assertEquals(756, result.getVariants().getEdges().get(0).getNode().getBinaryId());
        assertEquals("/media/balloons_tcm8-756.jpg", result.getVariants().getEdges().get(0).getNode().getPath());
        assertEquals("/media/balloons_tcm8-756.jpg", result.getVariants().getEdges().get(0).getNode().getUrl());
        assertEquals("http://localhost:8081/udp/content/binary/1/8/756", result.getVariants().getEdges().get(0).getNode().getDownloadUrl());
        assertEquals("[#def#]", result.getVariants().getEdges().get(0).getNode().getVariantId());

    }

    @Test
    public void getBinaryCompoonentByCmUri() throws Exception {
        BinaryComponent result = publicContentApi.getBinaryComponent(new CmUri("tcm:8-756-16"), null, new ContextData());

        assertEquals(BinaryComponent.class, result.getClass());
        assertEquals("b4e5c7c4-f04a-3d6d-898f-5886d0f648bd", result.getId());
        assertEquals(756, result.getItemId());
        assertEquals(1, result.getNamespaceId());
        assertEquals(8, result.getPublicationId());
        assertEquals("tcd:pub[8]/componentmeta[756]", result.getTitle());
        assertEquals(756, result.getVariants().getEdges().get(0).getNode().getBinaryId());
        assertEquals("/media/balloons_tcm8-756.jpg", result.getVariants().getEdges().get(0).getNode().getPath());
        assertEquals("/media/balloons_tcm8-756.jpg", result.getVariants().getEdges().get(0).getNode().getUrl());
        assertEquals("http://localhost:8081/udp/content/binary/1/8/756", result.getVariants().getEdges().get(0).getNode().getDownloadUrl());
        assertEquals("[#def#]", result.getVariants().getEdges().get(0).getNode().getVariantId());
    }

    @Test
    public void executePublicationsQuery() throws Exception {

        String query = prop.getProperty("PUBLICATION_QUERY");
        String graphQLJsonResponse = client.execute(query, 0);
        assertNotNull(graphQLJsonResponse);
    }

    @Test
    public void executeItemTypesQuery() throws Exception {

        String query = prop.getProperty("ITEMTYPES_QUERY_AND_VARIABLES");
        String graphQLJsonResponse = client.execute(query, 0);
        assertNotNull(graphQLJsonResponse);
    }

    @Test
    public void executeItemTypesQueryUsingGraphQLRequest() throws Exception {

        String query = prop.getProperty("ITEMTYPES_QUERY");

        String variables = prop.getProperty("ITEMTYPES_VARIABLES");
        HashMap<String, Object> variablesMap =
                new ObjectMapper().readValue(variables, HashMap.class);
        GraphQLRequest request = new GraphQLRequest(query, variablesMap);

        client = new DefaultGraphQLClient(prop.getProperty("GRAPHQL_SERVER_ENDPOINT"), null);
        String responsedata = client.execute(request);
        assertNotNull(responsedata);
    }

    @Test
    public void executeItemQueryPage() throws Exception {

        InputItemFilter filter = new InputItemFilter();
        filter.setNamespaceIds(Collections.singletonList(ContentNamespace.Sites.getNameSpaceValue()));
        filter.setItemTypes(Collections.singletonList(FilterItemType.PAGE));
        Pagination pagination = new Pagination();
        pagination.setFirst(10);

        ItemConnection result = publicContentApi.executeItemQuery(filter, null, pagination, "",
                ContentIncludeMode.INCLUDE_JSON, false, new ContextData());
        assertEquals(10, result.getEdges().size());
        assertEquals("MQ==", result.getEdges().get(0).getCursor());
        assertEquals(Page.class, result.getEdges().get(0).getNode().getClass());
        assertEquals("Publish Settings", result.getEdges().get(0).getNode().getTitle());
        assertEquals(ItemTypes.PAGE.getValue(), result.getEdges().get(0).getNode().getItemType());
    }

    @Test
    public void executeItemQueryComponent() throws Exception {

        InputItemFilter filter = new InputItemFilter();
        filter.setNamespaceIds(Collections.singletonList(ContentNamespace.Sites.getNameSpaceValue()));
        filter.setItemTypes(Collections.singletonList(FilterItemType.COMPONENT));
        Pagination pagination = new Pagination();
        pagination.setFirst(10);

        ItemConnection result = publicContentApi.executeItemQuery(filter, null, pagination, null,
                ContentIncludeMode.INCLUDE_DATA, false, new ContextData());

        assertEquals(10, result.getEdges().size());
        assertEquals("MQ==", result.getEdges().get(0).getCursor());
        assertEquals(Component.class, result.getEdges().get(0).getNode().getClass());
        assertEquals("Core", result.getEdges().get(0).getNode().getTitle());
        assertEquals(ItemTypes.COMPONENT.getValue(), result.getEdges().get(0).getNode().getItemType());
    }

    @Test
    public void executeItemQueryKeyword() throws Exception {

        InputItemFilter filter = new InputItemFilter();
        filter.setNamespaceIds(Collections.singletonList(ContentNamespace.Sites.getNameSpaceValue()));
        filter.setItemTypes(Collections.singletonList(FilterItemType.KEYWORD));
        Pagination pagination = new Pagination();
        pagination.setFirst(10);

        ItemConnection result = publicContentApi.executeItemQuery(filter, null, pagination, null,
                ContentIncludeMode.INCLUDE_DATA, false, new ContextData());

        assertEquals(10, result.getEdges().size());
        assertEquals("MQ==", result.getEdges().get(0).getCursor());
        assertEquals(Keyword.class, result.getEdges().get(0).getNode().getClass());
        assertEquals("001 Top-level Keyword 1", result.getEdges().get(0).getNode().getTitle());
        assertEquals(ItemTypes.KEYWORD.getValue(), result.getEdges().get(0).getNode().getItemType());
    }

    @Test
    public void executeItemQueryPublication() throws Exception {

        InputItemFilter filter = new InputItemFilter();
        filter.setNamespaceIds(Collections.singletonList(ContentNamespace.Sites.getNameSpaceValue()));
        filter.setItemTypes(Collections.singletonList(FilterItemType.PUBLICATION));
        Pagination pagination = new Pagination();
        pagination.setFirst(10);

        ItemConnection result = publicContentApi.executeItemQuery(filter, null, pagination, "",
                ContentIncludeMode.EXCLUDE, false, new ContextData());

        assertEquals(7, result.getEdges().size());
        assertEquals("MQ==", result.getEdges().get(0).getCursor());
        assertEquals(Publication.class, result.getEdges().get(0).getNode().getClass());
        assertEquals("400 Example Site", result.getEdges().get(0).getNode().getTitle());
        assertEquals(ItemTypes.PUBLICATION.getValue(), result.getEdges().get(0).getNode().getItemType());
    }

    @Test
    public void executeGetPageModelDataById() {
        assertNotNull(publicContentApi.getPageModelData(ContentNamespace.Sites, 1082, 640,
                ContentType.MODEL, DataModelType.DD4T, PageInclusion.INCLUDE, ContentIncludeMode.EXCLUDE, new ContextData()));
    }

    @Test
    public void executeGetPageModelDataByUri() {
        assertNotNull(publicContentApi.getPageModelData(ContentNamespace.Sites, 1082, "/example-legacy/index.html",
                ContentType.MODEL, DataModelType.R2, PageInclusion.INCLUDE, ContentIncludeMode.EXCLUDE, new ContextData()));
    }

    @Test
    public void executeGetEntityModelData() {
        assertNotNull(publicContentApi.getEntityModelData(ContentNamespace.Sites, 8, 1458, 9195,
                ContentType.MODEL, DataModelType.R2, DcpType.DEFAULT, ContentIncludeMode.EXCLUDE, new ContextData()));
    }

    @Test
    public void executeGetSitemap() {
        TaxonomySitemapItem result = publicContentApi.getSitemap(ContentNamespace.Sites, 8, 2,
                new ContextData());

        assertEquals("t2680", result.getId());
        assertEquals(4, result.getItems().size());
        assertEquals("Used for Taxonomy-based Navigation purposes", result.getDescription());
    }

    @Test
    public void executeGetSitemapSubtree() {
        TaxonomySitemapItem[] result = publicContentApi.getSitemapSubtree(ContentNamespace.Sites, 8,
                "t2680-k10019", 2, Ancestor.INCLUDE, new ContextData());

        assertEquals("t2680", result[0].getId());
        assertEquals(1, result[0].getItems().size());
        assertEquals("Used for Taxonomy-based Navigation purposes", result[0].getDescription());

    }

    @Test
    public void executeResolveBinaryLink() {
        String result = publicContentApi.resolveBinaryLink(ContentNamespace.Sites, 8, 756, "[#def#]", true);
        assertEquals("/media/balloons_tcm8-756.jpg", result);
    }

    @Test
    public void executeResolvePageLink() {
        String result = publicContentApi.resolvePageLink(ContentNamespace.Sites, 8, 4447, true);
        assertEquals("/system/include/content-tools.html", result);
    }

    @Test
    public void executeResolveComponentLink() {
        String result = publicContentApi.resolveComponentLink(ContentNamespace.Sites, 8, 3286, 640, 3292, true);
        assertEquals("/articles/all-articles.html", result);
    }

    @Test
    public void executeResolveDynamicComponentLink() {
        String result = publicContentApi.resolveDynamicComponentLink(ContentNamespace.Sites, 1082, 4569, 4565, 9195, true);
        assertEquals("/example-legacy/articles/news/news1.html", result);
    }

    @Test
    public void executegetPulbicationMapping() {
        PublicationMapping result = publicContentApi.getPublicationMapping(ContentNamespace.Sites, "http://localhost:8882/");

        assertEquals(PublicationMapping.class, result.getClass());
        assertEquals(5, result.getPublicationId());
        assertEquals("http", result.getProtocol());
        assertEquals("localhost", result.getDomain());
        assertEquals("8882", result.getPort());
        assertEquals("/", result.getPath());
        assertEquals(100, result.getPathScanDepth());
    }

    @Test
    public void executeGetPublication() {
        Publication result = publicContentApi.getPublication(ContentNamespace.Sites, 8, "", new ContextData());

        assertEquals(Publication.class, result.getClass());
        assertEquals("dec06688-3c29-36e6-9f91-710c6109aab5", result.getId());
        assertEquals(1, result.getNamespaceId());
        assertEquals(8, result.getPublicationId());
        assertEquals("400 Example Site", result.getTitle());
        assertEquals("/", result.getPublicationUrl());
        assertEquals("\\", result.getPublicationPath());
        assertEquals("\\media", result.getMultimediaPath());
        assertEquals("/media/", result.getMultimediaUrl());
    }

    @Test
    public void executeGetPublications() {
        Pagination pagination = new Pagination();
        pagination.setFirst(1);
        PublicationConnection result = publicContentApi.getPublications(ContentNamespace.Sites, pagination, null, "", new ContextData());

        assertEquals(PublicationConnection.class, result.getClass());
        assertEquals("MQ==", result.getEdges().get(0).getCursor());
        assertEquals("dec06688-3c29-36e6-9f91-710c6109aab5", result.getEdges().get(0).getNode().getId());
        assertEquals(8, result.getEdges().get(0).getNode().getPublicationId());
        assertEquals(1, result.getEdges().get(0).getNode().getNamespaceId());
        assertEquals("400 Example Site", result.getEdges().get(0).getNode().getTitle());
        assertEquals("/", result.getEdges().get(0).getNode().getPublicationUrl());
        assertEquals("\\", result.getEdges().get(0).getNode().getPublicationPath());
        assertEquals("\\media", result.getEdges().get(0).getNode().getMultimediaPath());
        assertEquals("/media/", result.getEdges().get(0).getNode().getMultimediaUrl());
    }

    @Test
    public void executeGenericRequest() throws JsonProcessingException, UnauthorizedException, GraphQLClientException {

        // Array of component Tcms
        String componentIds[] = { "236", "240", "292", "303", "343" };
        // Aliased GraphQL query to execute for each component Tcm
        String componentPresentationQuery = "\ncp{componentId}:componentPresentation(namespaceId:$namespaceId, publicationId:$publicationId, componentId: {componentId}, templateId:$templateId) {\n" +
                "    \t...ComponentPresentationFields" +
                "  }\n";
        // Construct complete query
        StringBuilder sb= new StringBuilder("query GetAllCps($namespaceId:Int!, $publicationId: Int!, $templateId: Int!) {\n");
        for (String componentId : componentIds) {
            sb.append(componentPresentationQuery.replace("{componentId}", componentId));
        }
        sb.append("}\n" +
                "\n" +
                "fragment ComponentPresentationFields on ComponentPresentation {\n" +
                "\trawContent(renderContent: true) {\n" +
                "\t\tdata\n" +
                "\t}\n" +
                "}");
        // Provide variables
        GraphQLRequest request = new GraphQLRequest(sb.toString(), ImmutableMap.of("namespaceId","1", "publicationId", "5", "templateId", "118"));
        // Execute
        String resultString = publicContentApi.execute(request);
        System.out.println(resultString);
        // Read JSON result to extract each component presentation
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultJson = mapper.readTree(resultString);
        JsonNode dataNode = resultJson.at("/data");
        Iterator<JsonNode> componentPresentations = dataNode.elements();
        componentPresentations.forEachRemaining(componentPresentation -> {
            System.out.println(componentPresentation.at("/rawContent").at("/data"));

            // Result could be used to retrieve EntityModel ...
            // JsonNode jsonNode = componentPresentation.at("/rawContent").at("/data");
            // EntityModelData entityModelData = objectMapper.treeToValue(jsonNode, EntityModelData.class);
            // return modelBuilderPipeline.createEntityModel(entityModelData, SvbEntityModel.class);
        });

        assertNotNull(resultJson);
    }
}