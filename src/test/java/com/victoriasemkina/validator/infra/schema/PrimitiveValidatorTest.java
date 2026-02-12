package com.victoriasemkina.validator.infra.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.victoriasemkina.validator.domain.model.ValidationIssue;
import com.victoriasemkina.validator.infra.schema.PrimitiveValidator;
import com.victoriasemkina.validator.infra.schema.ValidationContext;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PrimitiveValidator unit tests")
class PrimitiveValidatorTest {

    private PrimitiveValidator validator;
    private ObjectMapper objectMapper;
    private ValidationContext context;

    @BeforeEach
    void setUp() {
        validator = new PrimitiveValidator();
        objectMapper = new ObjectMapper();
        context = new ValidationContext("$", "/test", "GET");
    }

    @Test
    @DisplayName("should support string schema")
    void shouldSupportStringSchema() {
        // given
        StringSchema schema = new StringSchema();

        // when
        boolean result = validator.supports(schema);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should support integer schema")
    void shouldSupportIntegerSchema() {
        // given
        IntegerSchema schema = new IntegerSchema();

        // when
        boolean result = validator.supports(schema);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should validate correct string value")
    void shouldValidateCorrectStringValue() throws Exception {
        // given
        StringSchema schema = new StringSchema();
        var node = objectMapper.readTree("\"hello\"");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("should detect type mismatch for string")
    void shouldDetectTypeMismatchForString() throws Exception {
        // given
        StringSchema schema = new StringSchema();
        var node = objectMapper.readTree("123"); // number instead of string

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getSeverity()).isEqualTo(ValidationIssue.Severity.ERROR);
        assertThat(issues.get(0).getDescription()).contains("Type mismatch");
        assertThat(issues.get(0).getExpected()).contains("string");
        assertThat(issues.get(0).getActual()).contains("NUMBER");
    }

    @Test
    @DisplayName("should validate correct integer value")
    void shouldValidateCorrectIntegerValue() {
        // given
        IntegerSchema schema = new IntegerSchema();
        var node = JsonNodeFactory.instance.numberNode(42);

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("should detect type mismatch for integer")
    void shouldDetectTypeMismatchForInteger() throws Exception {
        // given
        IntegerSchema schema = new IntegerSchema();
        var node = objectMapper.readTree("\"not-a-number\"");

        // when
        List<ValidationIssue> issues = validator.validate(node, schema, context);

        // then
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getDescription()).contains("Type mismatch");
        assertThat(issues.get(0).getExpected()).contains("integer");
    }
}