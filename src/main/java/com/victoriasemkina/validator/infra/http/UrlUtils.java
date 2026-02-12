package com.victoriasemkina.validator.infra.http;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

/**
 * Utility methods for URL manipulation
 */
@UtilityClass
public class UrlUtils {

    /**
     * Safely joins base URL and path, avoiding double slashes
     *
     * @param baseUrl base URL (e.g. "https://api.example.com")
     * @param path path (e.g. "/users/{id}")
     * @return normalized full URL (e.g. "https://api.example.com/users/{id}")
     */
    public String buildFullUrl(String baseUrl, String path) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("Base URL cannot be empty");
        }
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        // Remove trailing slash from base URL
        String normalizedBaseUrl = baseUrl.replaceAll("/+$", "");

        // Remove leading slash from path if present (will be added explicitly)
        String normalizedPath = path.replaceAll("^/+", "");

        return normalizedBaseUrl + "/" + normalizedPath;
    }
}