package com.victoriasemkina.validator.config;

/**
 * Constants used across validation process
 */
public final class ValidationConstants {

    // HTTP
    public static final String HTTP_METHOD_GET = "GET";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String STATUS_200 = "200";

    // Paths
    public static final String PATH_SEPARATOR = "/";

    // Timeouts (milliseconds)
    public static final int DEFAULT_CONNECT_TIMEOUT = 10_000;
    public static final int DEFAULT_READ_TIMEOUT = 30_000;

    // JSON paths
    public static final String JSON_PATH_ROOT = "$";

    private ValidationConstants() {
        // Prevent instantiation
    }
}