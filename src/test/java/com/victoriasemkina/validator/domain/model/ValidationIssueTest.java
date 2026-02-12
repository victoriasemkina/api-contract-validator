package com.victoriasemkina.validator.domain.model;

import com.victoriasemkina.validator.domain.model.ValidationIssue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidationIssue unit tests")
class ValidationIssueTest {

    @Test
    @DisplayName("should create validation issue with all fields")
    void shouldCreateValidationIssueWithAllFields() {
        // given
        String method = "GET";
        String path = "/users";
        ValidationIssue.Severity severity = ValidationIssue.Severity.ERROR;
        String description = "Missing required field";
        String expected = "Field 'email' must be present";
        String actual = "Field is missing";

        // when
        ValidationIssue issue = ValidationIssue.builder()
                .method(method)
                .path(path)
                .severity(severity)
                .description(description)
                .expected(expected)
                .actual(actual)
                .build();

        // then
        assertThat(issue.getMethod()).isEqualTo(method);
        assertThat(issue.getPath()).isEqualTo(path);
        assertThat(issue.getSeverity()).isEqualTo(severity);
        assertThat(issue.getDescription()).isEqualTo(description);
        assertThat(issue.getExpected()).isEqualTo(expected);
        assertThat(issue.getActual()).isEqualTo(actual);
    }

    @Test
    @DisplayName("should have correct toString representation")
    void shouldHaveCorrectToStringRepresentation() {
        // given
        ValidationIssue issue = ValidationIssue.builder()
                .method("GET")
                .path("/users/1")
                .severity(ValidationIssue.Severity.ERROR)
                .description("Type mismatch at $.email")
                .expected("string")
                .actual("integer")
                .build();

        // when
        String result = issue.toString();

        // then
        assertThat(result)
                .contains("[ERROR]")
                .contains("GET /users/1")
                .contains("Type mismatch")
                .contains("expected: 'string'")
                .contains("actual: 'integer'");
    }

    @Test
    @DisplayName("should handle null expected/actual values gracefully")
    void shouldHandleNullExpectedActualValuesGracefully() {
        // given
        ValidationIssue issue = ValidationIssue.builder()
                .method("GET")
                .path("/test")
                .severity(ValidationIssue.Severity.WARNING)
                .description("Unexpected field")
                .expected(null)
                .actual(null)
                .build();

        // when / then — should not throw NullPointerException
        assertThat(issue.toString()).contains("[WARNING]").contains("Unexpected field");
    }
}