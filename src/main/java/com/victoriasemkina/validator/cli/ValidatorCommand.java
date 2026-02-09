package com.victoriasemkina.validator.cli;

import com.victoriasemkina.validator.model.ValidationIssue;
import com.victoriasemkina.validator.model.ValidationResult;
import com.victoriasemkina.validator.report.ReportGenerator;
import com.victoriasemkina.validator.report.ReportGeneratorFactory;
import com.victoriasemkina.validator.service.ContractValidatorService;
import com.victoriasemkina.validator.service.OpenApiParserService;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
        name = "api-contract-validator",
        description = "Validates API implementation against OpenAPI specification",
        mixinStandardHelpOptions = true,
        version = "1.0.0"
)
@Component
@Slf4j
public class ValidatorCommand implements Callable<Integer> {

    @Autowired
    private ReportGeneratorFactory reportGeneratorFactory;

    @Option(
            names = {"-s", "--spec"},
            required = true,
            description = "Path to OpenAPI spec file (YAML/JSON)"
    )
    private String specPath;

    @Option(
            names = {"-u", "--base-url"},
            required = true,
            description = "Base URL of the API to validate (e.g. https://api.example.com)"
    )
    private String baseUrl;

    @Option(
            names = {"-o", "--output"},
            description = "Output report file (default: console)"
    )
    private String outputPath;

    @Autowired
    private OpenApiParserService parserService;

    @Autowired
    private ContractValidatorService validatorService;

    @Override
    public Integer call() {
        log.info("Starting API contract validation");
        log.info("Spec file: {}", specPath);
        log.info("Base URL:  {}", baseUrl);

        this.specPath = specPath.trim();
        this.baseUrl = baseUrl.trim().replaceAll("/+$", "");

        try {
            // Step 1: Parse specification
            OpenAPI openAPI = parserService.parse(specPath);

            // Step 2: Validate endpoints
            ValidationResult result = validatorService.validate(openAPI, baseUrl);

            // Step 3: Generate report (auto-detect format)
            ReportGenerator generator = reportGeneratorFactory.getGenerator(outputPath);
            generator.generate(result, outputPath);

            return result.hasErrors() ? 1 : 0;
        } catch (Exception e) {
            log.error("Validation failed: {}", e.getMessage(), e);
            return 1;
        }
    }

    private void printResults(ValidationResult result) {
        log.info("");
        log.info("========================================");
        log.info("VALIDATION RESULTS");
        log.info("========================================");
        log.info("Base URL:    {}", result.getBaseUrl());
        log.info("Endpoints:   {}", result.getTotalEndpoints());
        log.info("Issues:      {}", result.getTotalIssues());
        log.info("Duration:    {} ms", result.getDurationMillis());
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