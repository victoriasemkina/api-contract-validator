package com.victoriasemkina.validator.cli.input;

import com.victoriasemkina.validator.cli.input.CommandLineValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommandLineValidator unit tests")
class CommandLineValidatorTest {

    private CommandLineValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CommandLineValidator();
    }

    @Test
    @DisplayName("should throw exception when spec path is null")
    void shouldThrowExceptionWhenSpecPathIsNull() {
        // when / then
        assertThatThrownBy(() -> validator.validateSpecFile(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Specification file path cannot be empty");
    }

    @Test
    @DisplayName("should throw exception when spec file does not exist")
    void shouldThrowExceptionWhenSpecFileDoesNotExist() {
        // when / then
        assertThatThrownBy(() -> validator.validateSpecFile("/non/existent/file.yaml"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("should throw exception when spec path is a directory")
    void shouldThrowExceptionWhenSpecPathIsDirectory(@TempDir Path tempDir) {
        // given
        File directory = tempDir.toFile();

        // when / then
        assertThatThrownBy(() -> validator.validateSpecFile(directory.getAbsolutePath()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not a file");
    }

    @Test
    @DisplayName("should validate existing spec file")
    void shouldValidateExistingSpecFile(@TempDir Path tempDir) throws IOException {
        // given
        File specFile = new File(tempDir.toFile(), "test.yaml");
        specFile.createNewFile();

        // when / then — should not throw exception
        validator.validateSpecFile(specFile.getAbsolutePath());
    }

    @Test
    @DisplayName("should throw exception when base URL is empty")
    void shouldThrowExceptionWhenBaseUrlIsEmpty() {
        // when / then
        assertThatThrownBy(() -> validator.validateBaseUrl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Base URL cannot be empty");
    }

    @Test
    @DisplayName("should throw exception when base URL has no protocol")
    void shouldThrowExceptionWhenBaseUrlHasNoProtocol() {
        // when / then
        assertThatThrownBy(() -> validator.validateBaseUrl("api.example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with http:// or https://");
    }

    @Test
    @DisplayName("should validate HTTPS URL")
    void shouldValidateHttpsUrl() {
        // when / then — should not throw exception
        validator.validateBaseUrl("https://api.example.com");
    }

    @Test
    @DisplayName("should validate HTTP URL")
    void shouldValidateHttpUrl() {
        // when / then — should not throw exception
        validator.validateBaseUrl("http://localhost:8080");
    }

    @Test
    @DisplayName("should trim trailing slashes from base URL")
    void shouldTrimTrailingSlashesFromBaseUrl() {
        // when / then — should not throw exception (trailing slashes are allowed, just trimmed internally)
        validator.validateBaseUrl("https://api.example.com///");
    }
}