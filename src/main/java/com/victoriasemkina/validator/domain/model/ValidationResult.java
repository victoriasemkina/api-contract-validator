package com.victoriasemkina.validator.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Final result of contract validation.
 */
@Data
public class ValidationResult {

    /** Timestamp of validation start */
    private LocalDateTime startedAt = LocalDateTime.now();

    /** Timestamp of validation end */
    private LocalDateTime finishedAt;

    /** Base URL of validated API */
    private String baseUrl;

    /** Total endpoints checked */
    private int totalEndpoints = 0;

    /** Total issues found */
    private int totalIssues = 0;

    /** List of all validation issues */
    private List<ValidationIssue> issues = new ArrayList<>();

    public void addIssue(ValidationIssue issue) {
        issues.add(issue);
        totalIssues++;
    }

    public boolean hasErrors() {
        return issues.stream()
                .anyMatch(issue -> issue.getSeverity() == ValidationIssue.Severity.ERROR);
    }

    public void finish() {
        this.finishedAt = LocalDateTime.now();
    }

    public long getDurationMillis() {
        if (finishedAt == null) return 0;
        return java.time.Duration.between(startedAt, finishedAt).toMillis();
    }
}