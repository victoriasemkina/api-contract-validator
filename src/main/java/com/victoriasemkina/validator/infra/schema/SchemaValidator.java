package com.victoriasemkina.validator.infra.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

/**
 * Interface for all schema validators
 * Each validator handles specific schema type
 */
public interface SchemaValidator {

    /**
     * Checks if this validator can handle the given schema type
     */
    boolean supports(Schema<?> schema);

    /**
     * Validates JSON node against schema
     *
     * @param node JSON node to validate
     * @param schema OpenAPI schema
     * @param context Validation context
     * @return List of validation issues (empty if valid)
     */
    List<ValidationIssue> validate(JsonNode node, Schema<?> schema, ValidationContext context);
}