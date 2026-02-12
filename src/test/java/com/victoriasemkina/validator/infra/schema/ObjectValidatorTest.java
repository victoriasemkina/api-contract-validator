package com.victoriasemkina.validator.infra.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ObjectValidator unit tests")
class ObjectValidatorTest {

    private ObjectValidator validator;
    private ObjectMapper objectMapper;
    private ValidationContext context;

    @BeforeEach
    void setUp() {
        // Create mock ObjectProvider that returns empty list (no nested validation needed for these tests)
        ObjectProvider<List<SchemaValidator>> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(new ArrayList<>());

        validator = new ObjectValidator(provider);
        objectMapper = new ObjectMapper();
        context = new ValidationContext("$", "/test", "GET");
    }

    @Test
    @DisplayName("should support ObjectSchema")
    void shouldSupportObjectSchema() {
        // given
        ObjectSchema schema = new ObjectSchema();

        // when
        boolean result = validator.supports(schema);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should detect non-object JSON as error")
    void shouldDetectNonObjectJsonAsError() throws Exception {
        // given
        ObjectSchema schema = new ObjectSchema();
        var node = objectMapper.readTree("\"not-an-object\"");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getSeverity()).isEqualTo(ValidationIssue.Severity.ERROR);
        assertThat(issues.get(0).getDescription()).contains("Expected object");
    }

    @Test
    @DisplayName("should detect missing required field")
    void shouldDetectMissingRequiredField() throws Exception {
        // given
        ObjectSchema schema = new ObjectSchema();
        schema.addRequiredItem("name");
        schema.addRequiredItem("email");

        // JSON without 'email' field
        var node = objectMapper.readTree("{\"name\": \"John\"}");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getDescription()).contains("Missing required field: email");
    }

    @Test
    @DisplayName("should validate object with all required fields")
    void shouldValidateObjectWithAllRequiredFields() throws Exception {
        // given
        ObjectSchema schema = new ObjectSchema();
        schema.addRequiredItem("id");
        schema.addRequiredItem("name");

        var node = objectMapper.readTree("{\"id\": 1, \"name\": \"John\", \"age\": 30}");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("should detect unexpected fields as warnings")
    void shouldDetectUnexpectedFieldsAsWarnings() throws Exception {
        // given
        ObjectSchema schema = new ObjectSchema();

        // Правильный способ добавления свойств в OpenAPI схему
        java.util.Map<String, io.swagger.v3.oas.models.media.Schema> properties = new java.util.HashMap<>();
        properties.put("id", new StringSchema());
        properties.put("name", new StringSchema());
        schema.setProperties(properties);

        // Note: 'age' is NOT in the schema
        var node = objectMapper.readTree("{\"id\": \"1\", \"name\": \"John\", \"age\": 30}");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getSeverity()).isEqualTo(ValidationIssue.Severity.WARNING);
        assertThat(issues.get(0).getDescription()).contains("Unexpected field");
        assertThat(issues.get(0).getDescription()).contains("age");
    }

    @Test
    @DisplayName("should handle empty object with no required fields")
    void shouldHandleEmptyObjectWithNoRequiredFields() throws Exception {
        // given
        ObjectSchema schema = new ObjectSchema(); // no required fields

        var node = objectMapper.readTree("{}");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).isEmpty();
    }
}