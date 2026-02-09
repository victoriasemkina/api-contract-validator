package com.victoriasemkina.validator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single validation issue found during contract validation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationIssue {

    /** HTTP method (GET, POST, etc.) */
    private String method;

    /** Endpoint path (e.g., "/api/users/{id}") */
    private String path;

    /** Issue severity */
    private Severity severity;

    /** Human-readable description */
    private String description;

    /** Expected value (optional) */
    private String expected;

    /** Actual value received (optional) */
    private String actual;

    public enum Severity {
        ERROR,    // Contract violation (required field missing, wrong type)
        WARNING   // Potential issue (optional field missing, unexpected field)
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ");
        sb.append(method).append(" ").append(path).append(": ");
        sb.append(description);
        if (expected != null && actual != null) {
            sb.append(" (expected: '").append(expected).append("', actual: '").append(actual).append("')");
        }
        return sb.toString();
    }
}