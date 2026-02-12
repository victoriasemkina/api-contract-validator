package com.victoriasemkina.validator.domain.service;

import com.victoriasemkina.validator.config.ValidationConstants;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import com.victoriasemkina.validator.domain.model.ValidationResult;
import com.victoriasemkina.validator.infra.http.UrlUtils;
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

        validateAllEndpoints(openAPI, baseUrl, result);

        result.finish();
        logSummary(result);

        return result;
    }

    private void validateAllEndpoints(OpenAPI openAPI, String baseUrl, ValidationResult result) {
        int index = 1;
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            log.info("Validating endpoint {}/{}: {}",
                    index, openAPI.getPaths().size(), entry.getKey());

            validatePath(result, baseUrl, entry.getKey(), entry.getValue());
            index++;
        }
    }

    private void validatePath(ValidationResult result, String baseUrl, String path, PathItem pathItem) {
        if (pathItem.getGet() != null) {
            validateGetEndpoint(result, baseUrl, path, pathItem.getGet());
        }
        // TODO: Add support for other HTTP methods
    }

    private void validateGetEndpoint(ValidationResult result, String baseUrl,
                                     String path, Operation operation) {
        String fullUrl = UrlUtils.buildFullUrl(baseUrl, path);
        log.debug("Sending GET request to {}", fullUrl);

        try {
            ResponseEntity<String> response = sendRequest(fullUrl);
            validateResponseStatus(result, path, response);

            if (response.getStatusCode() == HttpStatus.OK) {
                validateResponseBody(result, path, operation, response.getBody());
            }
        } catch (RestClientException e) {
            handleConnectionError(result, path, e);
        }
    }

    private ResponseEntity<String> sendRequest(String fullUrl) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
        long duration = System.currentTimeMillis() - startTime;

        log.debug("Response from GET {}: status={}, duration={}ms",
                extractPathFromUrl(fullUrl), response.getStatusCode(), duration);

        return response;
    }

    private void validateResponseStatus(ValidationResult result, String path,
                                        ResponseEntity<String> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            result.addIssue(new ValidationIssue(
                    ValidationConstants.HTTP_METHOD_GET,
                    path,
                    ValidationIssue.Severity.ERROR,
                    "Unexpected HTTP status",
                    "200 OK",
                    response.getStatusCode().toString()
            ));
        }
    }

    private void validateResponseBody(ValidationResult result, String path,
                                      Operation operation, String responseBody) {
        Schema<?> schema = extractJsonSchema(operation);
        if (schema == null) {
            log.warn("No JSON schema found for 200 response at {}", path);
            return;
        }

        List<ValidationIssue> issues = responseValidatorService.validateResponseBody(
                responseBody,
                schema,
                path
        );

        issues.forEach(result::addIssue);
    }

    private Schema<?> extractJsonSchema(Operation operation) {
        ApiResponse response200 = operation.getResponses() != null
                ? operation.getResponses().get(ValidationConstants.STATUS_200)
                : null;

        if (response200 == null || response200.getContent() == null) {
            return null;
        }

        Content content = response200.getContent();
        MediaType mediaType = content.get(ValidationConstants.CONTENT_TYPE_JSON);

        return mediaType != null ? mediaType.getSchema() : null;
    }

    private void handleConnectionError(ValidationResult result, String path, RestClientException e) {
        log.error("Request to GET {} failed: {}", path, e.getMessage());

        result.addIssue(new ValidationIssue(
                ValidationConstants.HTTP_METHOD_GET,
                path,
                ValidationIssue.Severity.ERROR,
                "Endpoint unreachable or request failed",
                "Successful response (200 OK)",
                "Connection error: " + e.getMessage()
        ));
    }

    private void logSummary(ValidationResult result) {
        long errorCount = result.getIssues().stream()
                .filter(issue -> issue.getSeverity() == ValidationIssue.Severity.ERROR)
                .count();

        log.info("Validation finished in {} ms. Issues found: {} (errors: {})",
                result.getDurationMillis(),
                result.getTotalIssues(),
                errorCount);
    }

    private String extractPathFromUrl(String fullUrl) {
        return fullUrl.replaceFirst("^https?://[^/]+", "");
    }
}