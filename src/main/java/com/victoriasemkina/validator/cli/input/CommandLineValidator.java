package com.victoriasemkina.validator.cli.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Validates command line arguments before execution
 */
@Slf4j
@Component
public class CommandLineValidator {

    /**
     * Validates that spec file exists and is readable
     */
    public void validateSpecFile(String specPath) {
        if (!StringUtils.hasText(specPath)) {
            throw new IllegalArgumentException("Specification file path cannot be empty");
        }

        File specFile = new File(specPath);
        if (!specFile.exists()) {
            throw new IllegalArgumentException("Specification file not found: " + specPath);
        }

        if (!specFile.isFile()) {
            throw new IllegalArgumentException("Spec path is not a file: " + specPath);
        }

        if (!specFile.canRead()) {
            throw new IllegalArgumentException("Cannot read specification file: " + specPath);
        }

        log.debug("Spec file validated: {}", specPath);
    }

    /**
     * Validates that base URL is valid
     */
    public void validateBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("Base URL cannot be empty");
        }

        baseUrl = baseUrl.trim();

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Base URL must start with http:// or https://");
        }

        // Remove trailing slashes
        baseUrl = baseUrl.replaceAll("/+$", "");

        if (baseUrl.length() <= 8) { // "https://" = 8 chars
            throw new IllegalArgumentException("Base URL is too short: " + baseUrl);
        }

        log.debug("Base URL validated: {}", baseUrl);
    }
}