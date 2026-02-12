package com.victoriasemkina.validator.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import com.victoriasemkina.validator.infra.schema.SchemaValidator;
import com.victoriasemkina.validator.infra.schema.ValidationContext;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Main response validation service
 * Coordinates validation across specialized validators
 */
@Slf4j
@Service
public class ResponseValidatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<SchemaValidator> validators;

    @Autowired
    public ResponseValidatorService(List<SchemaValidator> validators) {
        this.validators = validators;
    }

    /**
     * Validates response body against OpenAPI schema
     */
    public List<ValidationIssue> validateResponseBody(String responseBody,
                                                      Schema<?> schema,
                                                      String endpointPath) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (responseBody == null || responseBody.trim().isEmpty()) {
            issues.add(createErrorIssue("GET", endpointPath,
                    "Response body is empty",
                    "Valid JSON object/array",
                    "Empty response"));
            return issues;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            ValidationContext context = new ValidationContext(
                    "$",
                    endpointPath,
                    "GET"
            );

            issues.addAll(validateRoot(rootNode, schema, context));
        } catch (Exception e) {
            log.error("Failed to parse response body as JSON for endpoint: {}", endpointPath, e);
            issues.add(createErrorIssue("GET", endpointPath,
                    "Failed to parse response body as JSON",
                    "Valid JSON",
                    "Parse error: " + e.getMessage()));
        }

        return issues;
    }

    private List<ValidationIssue> validateRoot(JsonNode node, Schema<?> schema,
                                               ValidationContext context) {
        for (SchemaValidator validator : validators) {
            if (validator.supports(schema)) {
                return validator.validate(node, schema, context);
            }
        }

        log.warn("No validator found for schema type: {}",
                schema != null ? schema.getType() : "null");
        return new ArrayList<>();
    }

    private ValidationIssue createErrorIssue(String method, String endpointPath,
                                             String description, String expected, String actual) {
        return new ValidationIssue(
                method,
                endpointPath,
                ValidationIssue.Severity.ERROR,
                description,
                expected,
                actual
        );
    }
}