package com.victoriasemkina.validator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victoriasemkina.validator.model.ValidationIssue;
import io.swagger.v3.oas.models.media.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ResponseValidatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Validates response body against OpenAPI schema
     *
     * @param responseBody JSON response body
     * @param schema OpenAPI schema from specification
     * @param path Endpoint path (for error reporting)
     * @return List of validation issues (empty if valid)
     */
    public List<ValidationIssue> validateResponseBody(String responseBody, Schema<?> schema, String path) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (responseBody == null || responseBody.trim().isEmpty()) {
            issues.add(new ValidationIssue(
                    "GET",
                    path,
                    ValidationIssue.Severity.ERROR,
                    "Response body is empty",
                    "Valid JSON object/array",
                    "Empty response"
            ));
            return issues;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            validateNode(rootNode, schema, "$", path, issues);
        } catch (Exception e) {
            issues.add(new ValidationIssue(
                    "GET",
                    path,
                    ValidationIssue.Severity.ERROR,
                    "Failed to parse response body as JSON",
                    "Valid JSON",
                    "Parse error: " + e.getMessage()
            ));
            log.error("JSON parsing error for path {}: {}", path, e.getMessage());
        }

        return issues;
    }

    private void validateNode(JsonNode node, Schema<?> schema, String fieldPath,
                              String endpointPath, List<ValidationIssue> issues) {
        // Check required fields
        if (schema instanceof ObjectSchema || schema instanceof ComposedSchema) {
            validateObject(node, (ObjectSchema) schema, fieldPath, endpointPath, issues);
        } else if (schema instanceof ArraySchema) {
            validateArray(node, (ArraySchema) schema, fieldPath, endpointPath, issues);
        }
        // For primitive types, basic validation is enough
    }

    private void validateObject(JsonNode node, ObjectSchema schema, String fieldPath,
                                String endpointPath, List<ValidationIssue> issues) {
        if (!node.isObject()) {
            issues.add(new ValidationIssue(
                    "GET",
                    endpointPath,
                    ValidationIssue.Severity.ERROR,
                    "Expected object at " + fieldPath,
                    "JSON object",
                    "Type: " + node.getNodeType()
            ));
            return;
        }

        // Check required fields
        List<String> required = schema.getRequired();
        if (required != null) {
            for (String fieldName : required) {
                if (!node.has(fieldName)) {
                    issues.add(new ValidationIssue(
                            "GET",
                            endpointPath,
                            ValidationIssue.Severity.ERROR,
                            "Missing required field: " + fieldPath + "." + fieldName,
                            "Field '" + fieldName + "' must be present",
                            "Field is missing"
                    ));
                } else {
                    // Validate field type if schema is available
                    Schema<?> fieldSchema = schema.getProperties() != null
                            ? schema.getProperties().get(fieldName)
                            : null;

                    if (fieldSchema != null) {
                        validateField(node.get(fieldName), fieldSchema,
                                fieldPath + "." + fieldName, endpointPath, issues);
                    }
                }
            }
        }

        // Check for unexpected fields (optional)
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (schema.getProperties() != null && !schema.getProperties().containsKey(fieldName)) {
                issues.add(new ValidationIssue(
                        "GET",
                        endpointPath,
                        ValidationIssue.Severity.WARNING,
                        "Unexpected field in response: " + fieldPath + "." + fieldName,
                        "Only documented fields",
                        "Field not in specification"
                ));
            }
        }
    }

    private void validateArray(JsonNode node, ArraySchema schema, String fieldPath,
                               String endpointPath, List<ValidationIssue> issues) {
        if (!node.isArray()) {
            issues.add(new ValidationIssue(
                    "GET",
                    endpointPath,
                    ValidationIssue.Severity.ERROR,
                    "Expected array at " + fieldPath,
                    "JSON array",
                    "Type: " + node.getNodeType()
            ));
            return;
        }

        // Validate array items if schema is available
        Schema<?> itemsSchema = schema.getItems();
        if (itemsSchema != null) {
            for (int i = 0; i < node.size(); i++) {
                validateField(node.get(i), itemsSchema,
                        fieldPath + "[" + i + "]", endpointPath, issues);
            }
        }
    }

    private void validateField(JsonNode node, Schema<?> schema, String fieldPath,
                               String endpointPath, List<ValidationIssue> issues) {
        if (schema == null) {
            return;
        }

        // Handle nested objects
        if (schema instanceof ObjectSchema) {
            validateObject(node, (ObjectSchema) schema, fieldPath, endpointPath, issues);
        }
        // Handle arrays
        else if (schema instanceof ArraySchema) {
            validateArray(node, (ArraySchema) schema, fieldPath, endpointPath, issues);
        }
        // Basic type validation
        else {
            validatePrimitiveType(node, schema, fieldPath, endpointPath, issues);
        }
    }

    private void validatePrimitiveType(JsonNode node, Schema<?> schema, String fieldPath,
                                       String endpointPath, List<ValidationIssue> issues) {
        String expectedType = schema.getType();

        if (expectedType == null) {
            return; // Skip if type is not specified
        }

        boolean typeMismatch = false;
        String actualType = "";

        switch (expectedType) {
            case "string":
                if (!node.isTextual()) {
                    typeMismatch = true;
                    actualType = node.getNodeType().toString();
                }
                break;
            case "integer":
                if (!node.isInt() && !node.isLong()) {
                    typeMismatch = true;
                    actualType = node.getNodeType().toString();
                }
                break;
            case "number":
                if (!node.isNumber()) {
                    typeMismatch = true;
                    actualType = node.getNodeType().toString();
                }
                break;
            case "boolean":
                if (!node.isBoolean()) {
                    typeMismatch = true;
                    actualType = node.getNodeType().toString();
                }
                break;
            case "object":
                if (!node.isObject()) {
                    typeMismatch = true;
                    actualType = node.getNodeType().toString();
                }
                break;
            case "array":
                if (!node.isArray()) {
                    typeMismatch = true;
                    actualType = node.getNodeType().toString();
                }
                break;
        }

        if (typeMismatch) {
            issues.add(new ValidationIssue(
                    "GET",
                    endpointPath,
                    ValidationIssue.Severity.ERROR,
                    "Type mismatch at " + fieldPath,
                    "Expected type: " + expectedType,
                    "Actual type: " + actualType
            ));
        }
    }
}