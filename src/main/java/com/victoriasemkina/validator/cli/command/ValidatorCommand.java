package com.victoriasemkina.validator.cli.command;

import com.victoriasemkina.validator.domain.model.ValidationResult;
import com.victoriasemkina.validator.infra.report.ReportGeneratorFactory;
import com.victoriasemkina.validator.domain.service.ContractValidatorService;
import com.victoriasemkina.validator.infra.openapi.OpenApiParserService;
import com.victoriasemkina.validator.cli.input.CommandLineValidator;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    @Autowired
    private ReportGeneratorFactory reportGeneratorFactory;

    @Autowired
    private CommandLineValidator commandLineValidator;

    @Override
    public Integer call() {
        try {
            log.info("🚀 Starting API contract validation");
            log.info("Spec file: {}", specPath);
            log.info("Base URL:  {}", baseUrl);

            // Validate command line arguments
            validateArguments();

            // Parse specification
            OpenAPI openAPI = parserService.parse(specPath);

            // Validate endpoints
            ValidationResult result = validatorService.validate(openAPI, baseUrl);

            // Generate report
            generateReport(result);

            return result.hasErrors() ? 1 : 0;
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid arguments: {}", e.getMessage());
            return 1;
        } catch (Exception e) {
            log.error("❌ Validation failed: {}", e.getMessage(), e);
            return 1;
        }
    }

    private void validateArguments() {
        commandLineValidator.validateSpecFile(specPath);
        commandLineValidator.validateBaseUrl(baseUrl);
        log.info("✅ Arguments validated successfully");
    }

    private void generateReport(ValidationResult result) throws Exception {
        var generator = reportGeneratorFactory.getGenerator(outputPath);
        generator.generate(result, outputPath);

        if (outputPath != null && !outputPath.trim().isEmpty()) {
            log.info("📄 Report saved to: {}", outputPath);
        }
    }
}