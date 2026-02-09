package com.victoriasemkina.validator.report;

import com.victoriasemkina.validator.model.ValidationResult;

import java.io.IOException;

/**
 * Interface for generating validation reports in different formats
 */
public interface ReportGenerator {

    /**
     * Generates report from validation result
     *
     * @param result Validation result
     * @param outputPath Output file path (null for console output)
     * @throws IOException if report generation fails
     */
    void generate(ValidationResult result, String outputPath) throws IOException;
}