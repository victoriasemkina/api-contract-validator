package com.victoriasemkina.validator.infra.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ArrayValidator implements SchemaValidator {

    private final ObjectProvider<List<SchemaValidator>> validatorsProvider;

    public ArrayValidator(ObjectProvider<List<SchemaValidator>> validatorsProvider) {
        this.validatorsProvider = validatorsProvider;
    }

    @Override
    public boolean supports(Schema<?> schema) {
        return schema instanceof ArraySchema;
    }

    @Override
    public List<ValidationIssue> validate(JsonNode node, Schema<?> schema, ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (!node.isArray()) {
            issues.add(createErrorIssue(context,
                    "Expected array",
                    "JSON array",
                    "Type: " + node.getNodeType()));
            return issues;
        }

        ArraySchema arraySchema = (ArraySchema) schema;
        Schema<?> itemsSchema = arraySchema.getItems();

        if (itemsSchema != null) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode itemNode = node.get(i);
                ValidationContext itemContext = new ValidationContext(
                        context.getFieldPath() + "[" + i + "]",
                        context.getEndpointPath(),
                        context.getHttpMethod()
                );

                issues.addAll(validateItem(itemNode, itemsSchema, itemContext));
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateItem(JsonNode itemNode, Schema<?> itemsSchema,
                                               ValidationContext context) {
        // Получаем список валидаторов только при необходимости (лениво)
        List<SchemaValidator> validators = validatorsProvider.getObject();

        for (SchemaValidator validator : validators) {
            if (validator.supports(itemsSchema)) {
                return validator.validate(itemNode, itemsSchema, context);
            }
        }

        log.warn("No validator found for schema type: {}", itemsSchema.getType());
        return new ArrayList<>();
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