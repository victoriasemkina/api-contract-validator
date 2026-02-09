package com.victoriasemkina.validator.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ReportGeneratorFactory {

    private final Map<String, ReportGenerator> generators = new HashMap<>();

    @Autowired
    public ReportGeneratorFactory(ConsoleReportGenerator consoleGenerator,
                                  HtmlReportGenerator htmlGenerator) {
        generators.put("console", consoleGenerator);
        generators.put("html", htmlGenerator);
    }

    /**
     * Gets report generator based on output file extension
     */
    public ReportGenerator getGenerator(String outputPath) {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            log.info("No output path specified, using console report");
            return generators.get("console");
        }

        if (outputPath.toLowerCase().endsWith(".html") || outputPath.toLowerCase().endsWith(".htm")) {
            log.info("HTML output detected, using HTML report generator");
            return generators.get("html");
        }

        log.info("Unknown output format, using console report");
        return generators.get("console");
    }
}