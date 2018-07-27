package com.sdl.web.pca.client;

/**
 * Represents a GraphQL Exception.
 */
public class GraphQLException extends Exception {
    /**
     * @param message The error message.
     */
    public GraphQLException(String message) {
        super(message);
    }
}