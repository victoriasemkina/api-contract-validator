package com.victoriasemkina.validator.infra.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates primitive types: string, integer, number, boolean
 */
@Slf4j
@Component
public class PrimitiveValidator implements SchemaValidator {

    @Override
    public boolean supports(Schema<?> schema) {
        if (schema == null || schema.getType() == null) {
            return false;
        }

        String type = schema.getType();
        return "string".equals(type) || "integer".equals(type) ||
                "number".equals(type) || "boolean".equals(type);
    }

    @Override
    public List<ValidationIssue> validate(JsonNode node, Schema<?> schema, ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (node == null || node.isNull()) {
            if (Boolean.TRUE.equals(schema.getNullable())) {
                return issues; // Nullable field is allowed to be null
            }
            issues.add(createErrorIssue(context,
                    "Field is null but not marked as nullable",
                    "Non-null value", "null"));
            return issues;
        }

        String expectedType = schema.getType();
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
        }

        if (typeMismatch) {
            issues.add(createErrorIssue(context,
                    "Type mismatch",
                    "Expected type: " + expectedType,
                    "Actual type: " + actualType));
        }

        return issues;
    }

    private ValidationIssue createErrorIssue(ValidationContext context,
                                             String description,
                                             String expected,
                                             String actual) {
        return new ValidationIssue(
                context.getHttpMethod() != null ? context.getHttpMethod() : "GET",
                context.getEndpointPath(),
                ValidationIssue.Severity.ERROR,
                description + " at " + context.getFieldPath(),
                expected,
                actual
        );
    }
}