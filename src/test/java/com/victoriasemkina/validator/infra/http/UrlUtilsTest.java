package com.victoriasemkina.validator.infra.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UrlUtils unit tests")
class UrlUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "https://api.example.com, /users, https://api.example.com/users",
            "https://api.example.com/, /users, https://api.example.com/users",
            "https://api.example.com, users, https://api.example.com/users",
            "https://api.example.com/v1/, /users/123, https://api.example.com/v1/users/123",
            "http://localhost:8080, /api/data, http://localhost:8080/api/data"
    })
    @DisplayName("should build full URL correctly")
    void shouldBuildFullUrlCorrectly(String baseUrl, String path, String expectedUrl) {
        // when
        String result = UrlUtils.buildFullUrl(baseUrl, path);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("should throw exception when base URL is empty")
    void shouldThrowExceptionWhenBaseUrlIsEmpty() {
        // when / then
        assertThatThrownBy(() -> UrlUtils.buildFullUrl("", "/users"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Base URL cannot be empty");
    }

    @Test
    @DisplayName("should throw exception when path is empty")
    void shouldThrowExceptionWhenPathIsEmpty() {
        // when / then
        assertThatThrownBy(() -> UrlUtils.buildFullUrl("https://api.example.com", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Path cannot be empty");
    }

    @Test
    @DisplayName("should handle URL with query parameters")
    void shouldHandleUrlWithQueryParameters() {
        // given
        String baseUrl = "https://api.example.com";
        String path = "/users?id=42";

        // when
        String result = UrlUtils.buildFullUrl(baseUrl, path);

        // then
        assertThat(result).isEqualTo("https://api.example.com/users?id=42");
    }
}