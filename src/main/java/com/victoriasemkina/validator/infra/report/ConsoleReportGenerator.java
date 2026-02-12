package com.victoriasemkina.validator.infra.report;

import com.victoriasemkina.validator.domain.model.ValidationIssue;
import com.victoriasemkina.validator.domain.model.ValidationResult;
import com.victoriasemkina.validator.domain.port.ReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ConsoleReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void generate(ValidationResult result, String outputPath) throws IOException {
        log.info("");
        log.info("========================================");
        log.info("VALIDATION RESULTS");
        log.info("========================================");
        log.info("Base URL:    {}", result.getBaseUrl());
        log.info("Endpoints:   {}", result.getTotalEndpoints());
        log.info("Issues:      {}", result.getTotalIssues());
        log.info("Duration:    {} ms", result.getDurationMillis());
        log.info("Started:     {}", result.getStartedAt().format(TIME_FORMATTER));
        log.info("Finished:    {}", result.getFinishedAt().format(TIME_FORMATTER));
        log.info("========================================");

        if (result.getTotalIssues() > 0) {
            log.info("");
            log.info("ISSUES FOUND:");
            log.info("------------");

            for (ValidationIssue issue : result.getIssues()) {
                log.info("{}", issue);
            }
        } else {
            log.info("");
            log.info("✅ All endpoints passed validation!");
        }

        log.info("========================================");
    }
}