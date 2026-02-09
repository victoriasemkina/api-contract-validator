package com.victoriasemkina.validator.service;

import com.victoriasemkina.validator.model.ValidationIssue;
import com.victoriasemkina.validator.model.ValidationResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ContractValidatorService {

    private final RestTemplate restTemplate;
    private final ResponseValidatorService responseValidatorService;

    @Autowired
    public ContractValidatorService(RestTemplate restTemplate,
                                    ResponseValidatorService responseValidatorService) {
        this.restTemplate = restTemplate;
        this.responseValidatorService = responseValidatorService;
    }

    public ValidationResult validate(OpenAPI openAPI, String baseUrl) {
        ValidationResult result = new ValidationResult();
        result.setBaseUrl(baseUrl);
        result.setTotalEndpoints(openAPI.getPaths().size());

        log.info("Starting validation of {} endpoints against {}",
                openAPI.getPaths().size(), baseUrl);

        int endpointIndex = 1;
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();

            log.info("Validating endpoint {}/{}: {}",
                    endpointIndex, openAPI.getPaths().size(), path);

            validatePath(result, baseUrl, path, pathItem);
            endpointIndex++;
        }

        result.finish();
        log.info("Validation finished in {} ms. Issues found: {} (errors: {})",
                result.getDurationMillis(),
                result.getTotalIssues(),
                result.getIssues().stream()
                        .filter(issue -> issue.getSeverity() == ValidationIssue.Severity.ERROR)
                        .count());

        return result;
    }

    private void validatePath(ValidationResult result, String baseUrl, String path, PathItem pathItem) {
        if (pathItem.getGet() != null) {
            validateEndpoint(result, baseUrl, path, "GET", pathItem.getGet());
        }
    }

    private void validateEndpoint(ValidationResult result, String baseUrl, String path,
                                  String method, Operation operation) {
        // Убираем лишние слеши между базовым URL и путём
        String fullUrl = baseUrl + (path.startsWith("/") ? "" : "/") + path;
        fullUrl = fullUrl.replaceAll("//+", "/").replace(":/", "://"); // Исправляем двойные слеши

        log.debug("Sending {} request to {}", method, fullUrl);

        try {
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.debug("Response from {} {}: status={}, duration={}ms",
                    method, path, response.getStatusCode(), duration);

            // Validate HTTP status
            if (response.getStatusCode() != HttpStatus.OK) {
                result.addIssue(new ValidationIssue(
                        method,
                        path,
                        ValidationIssue.Severity.ERROR,
                        "Unexpected HTTP status",
                        "200 OK",
                        response.getStatusCode().toString()
                ));
                return;
            }

            // Validate response body
            String responseBody = response.getBody();
            validateResponseBody(result, path, operation, responseBody);

            log.debug("✓ {} {} returned {}", method, path, response.getStatusCode());

        } catch (RestClientException e) {
            log.error("Request to {} {} failed: {}", method, path, e.getMessage(), e);

            result.addIssue(new ValidationIssue(
                    method,
                    path,
                    ValidationIssue.Severity.ERROR,
                    "Endpoint unreachable or request failed",
                    "Successful response (200 OK)",
                    "Connection error: " + e.getMessage()
            ));
        }
    }

    private void validateResponseBody(ValidationResult result, String path,
                                      Operation operation, String responseBody) {
        // Get 200 response schema from OpenAPI spec
        ApiResponse response200 = operation.getResponses() != null
                ? operation.getResponses().get("200")
                : null;

        if (response200 == null) {
            log.warn("No 200 response schema defined for {}", path);
            return;
        }

        Content content = response200.getContent();
        if (content == null || content.isEmpty()) {
            log.warn("No content schema defined for 200 response at {}", path);
            return;
        }

        // Get JSON schema (application/json)
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            log.warn("No application/json schema for {}", path);
            return;
        }

        Schema<?> schema = mediaType.getSchema();
        if (schema == null) {
            log.warn("No schema defined for application/json at {}", path);
            return;
        }

        // Validate response body against schema
        List<ValidationIssue> issues = responseValidatorService.validateResponseBody(
                responseBody,
                schema,
                path
        );

        // Add all issues to result
        for (ValidationIssue issue : issues) {
            result.addIssue(issue);
        }
    }
}