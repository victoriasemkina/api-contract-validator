package com.victoriasemkina.validator.infra.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ObjectValidator implements SchemaValidator {

    private final ObjectProvider<List<SchemaValidator>> validatorsProvider;

    public ObjectValidator(ObjectProvider<List<SchemaValidator>> validatorsProvider) {
        this.validatorsProvider = validatorsProvider;
    }

    @Override
    public boolean supports(Schema<?> schema) {
        return schema instanceof ObjectSchema;
    }

    @Override
    public List<ValidationIssue> validate(JsonNode node, Schema<?> schema, ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (!node.isObject()) {
            issues.add(createErrorIssue(context,
                    "Expected object",
                    "JSON object",
                    "Type: " + node.getNodeType()));
            return issues;
        }

        ObjectSchema objectSchema = (ObjectSchema) schema;

        issues.addAll(validateRequiredFields(node, objectSchema, context));
        issues.addAll(validateFieldTypes(node, objectSchema, context));
        issues.addAll(checkUnexpectedFields(node, objectSchema, context));

        return issues;
    }

    private List<ValidationIssue> validateRequiredFields(JsonNode node, ObjectSchema schema,
                                                         ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        List<String> required = schema.getRequired();
        if (required != null) {
            for (String fieldName : required) {
                if (!node.has(fieldName)) {
                    issues.add(createErrorIssue(context,
                            "Missing required field: " + fieldName,
                            "Field '" + fieldName + "' must be present",
                            "Field is missing"));
                }
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateFieldTypes(JsonNode node, ObjectSchema schema,
                                                     ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                Schema<?> fieldSchema = entry.getValue();

                if (node.has(fieldName)) {
                    JsonNode fieldValue = node.get(fieldName);
                    ValidationContext fieldContext = new ValidationContext(
                            context.getFieldPath() + "." + fieldName,
                            context.getEndpointPath(),
                            context.getHttpMethod()
                    );

                    issues.addAll(validateField(fieldValue, fieldSchema, fieldContext));
                }
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateField(JsonNode node, Schema<?> schema,
                                                ValidationContext context) {
        // Получаем список валидаторов только при необходимости (лениво)
        List<SchemaValidator> validators = validatorsProvider.getObject();

        for (SchemaValidator validator : validators) {
            if (validator.supports(schema)) {
                return validator.validate(node, schema, context);
            }
        }

        if (schema.getType() == null) {
            log.debug("Schema type not specified for field: {}", context.getFieldPath());
        }

        return new ArrayList<>();
    }

    private List<ValidationIssue> checkUnexpectedFields(JsonNode node, ObjectSchema schema,
                                                        ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (!properties.containsKey(fieldName)) {
                    issues.add(new ValidationIssue(
                            context.getHttpMethod() != null ? context.getHttpMethod() : "GET",
                            context.getEndpointPath(),
                            ValidationIssue.Severity.WARNING,
                            "Unexpected field in response: " + context.getFieldPath() + "." + fieldName,
                            "Only documented fields",
                            "Field not in specification"
                    ));
                }
            }
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