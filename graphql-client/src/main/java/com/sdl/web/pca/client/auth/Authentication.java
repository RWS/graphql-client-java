package com.sdl.web.pca.client.auth;

import org.apache.http.HttpRequest;

/**
 * Provides possibility to add authentication headers to request
 */
public interface Authentication {

    /**
     * Adds authentication headers to given http request.
     * @param request Instance of the current HTTP request
     */
    void applyManualAuthentication(HttpRequest request);

}
