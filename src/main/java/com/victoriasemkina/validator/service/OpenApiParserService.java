package com.victoriasemkina.validator.service;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class OpenApiParserService {

    /**
     * Parses OpenAPI specification from file
     *
     * @param specPath path to OpenAPI file (YAML or JSON)
     * @return OpenAPI object
     * @throws RuntimeException if file not found or parsing failed
     */
    public OpenAPI parse(String specPath) {
        File specFile = new File(specPath);

        if (!specFile.exists()) {
            throw new RuntimeException("Specification file not found: " + specPath);
        }

        log.info("Reading specification: {}", specPath);

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        SwaggerParseResult result = new OpenAPIV3Parser()
                .readLocation(specFile.getAbsolutePath(), null, options);

        if (result.getOpenAPI() == null) {
            String errors = String.join("\n", result.getMessages());
            throw new RuntimeException("OpenAPI parsing error:\n" + errors);
        }

        OpenAPI openAPI = result.getOpenAPI();
        log.info("Specification loaded: {} v{}",
                openAPI.getInfo().getTitle(),
                openAPI.getInfo().getVersion());
        log.info("Endpoints found: {}", openAPI.getPaths().size());

        return openAPI;
    }
}