package com.sdl.web.pca.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdl.web.pca.client.auth.Authentication;
import com.sdl.web.pca.client.exception.GraphQLClientException;
import com.sdl.web.pca.client.exception.UnauthorizedException;
import com.sdl.web.pca.client.request.GraphQLRequest;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultGraphQLClient implements GraphQLClient {
    private static final Logger LOG = getLogger(DefaultGraphQLClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Authentication auth;
    private CloseableHttpClient httpClient = null;
    private final String endpoint;
    private final ConcurrentHashMap<String, String> defaultHeaders = new ConcurrentHashMap<>();

    public DefaultGraphQLClient(String endpoint, Map<String, String> defaultHeaders) {
        this(endpoint, defaultHeaders, null);
    }

    public DefaultGraphQLClient(String endpoint, Map<String, String> defaultHeaders, Authentication auth) {
        this.endpoint = endpoint;
        if (defaultHeaders != null) {
            this.defaultHeaders.putAll(defaultHeaders);
        }
        this.auth = auth;
    }

    public CloseableHttpClient createHttpClient() {
        return HttpClients.createDefault();
    }

    @Override
    public String execute(String jsonEntity) throws UnauthorizedException, GraphQLClientException {
        return execute(jsonEntity, 0);
    }

    @Override
    public String execute(String jsonEntity, int timeout) throws UnauthorizedException, GraphQLClientException {
        LOG.debug("Requested entity: {}", jsonEntity);
        HttpPost httpPost = new HttpPost(endpoint);
        defaultHeaders.forEach((key, value) -> httpPost.addHeader(key, value));

        if (timeout > 0) {
            RequestConfig params = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
            httpPost.setConfig(params);
        }

        StringEntity entity = new StringEntity(jsonEntity, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        if (auth != null) {
            auth.applyManualAuthentication(httpPost);
        }

        //Execute and get the response.
        if (httpClient == null) httpClient = createHttpClient();
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            InputStream contentStream = response.getEntity().getContent();
            String contentString = IOUtils.toString(contentStream, "UTF-8");
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                if (response.getStatusLine().getStatusCode() == SC_UNAUTHORIZED) {
                    throw new UnauthorizedException("Unable to retrieve requested entity, message: " + contentString);
                }
                LOG.error("Response code is '" + response.getStatusLine().getStatusCode() +
                        "'. The response message: " + contentString);
                throw new GraphQLClientException("Unable to retrieve requested entity");
            }
            LOG.debug("Returned message: {}", contentString);
            return contentString;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLClientException("Exception during requesting entity: " + jsonEntity, e);
        }
    }

    @Override
    public String execute(GraphQLRequest request) throws UnauthorizedException, GraphQLClientException {
        try {
            String stringRequest = MAPPER.writeValueAsString(request);
            return execute(stringRequest, request.getTimeout());
        } catch (JsonProcessingException e) {
            throw new GraphQLClientException("Unable to serialize request: " + request.toString(), e);
        }
    }

    @Override
    public void addDefaultHeader(String header, String value) {
        this.defaultHeaders.put(header, value);
    }
}
