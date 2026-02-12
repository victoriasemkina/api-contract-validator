package com.victoriasemkina.validator.infra.report;

import com.victoriasemkina.validator.domain.model.ValidationIssue;
import com.victoriasemkina.validator.domain.model.ValidationResult;
import com.victoriasemkina.validator.domain.port.ReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class HtmlReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void generate(ValidationResult result, String outputPath) throws IOException {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            outputPath = "validation-report.html";
        }

        log.info("Generating HTML report to: {}", outputPath);

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(buildHtmlReport(result));
            log.info("HTML report saved successfully: {}", outputPath);
        }
    }

    private String buildHtmlReport(ValidationResult result) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>API Contract Validation Report</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append("        .summary { background: #ecf0f1; padding: 20px; border-radius: 5px; margin-bottom: 30px; }\n");
        html.append("        .summary-item { display: flex; justify-content: space-between; margin: 10px 0; }\n");
        html.append("        .summary-label { font-weight: bold; color: #34495e; }\n");
        html.append("        .summary-value { color: #2c3e50; }\n");
        html.append("        .status { padding: 5px 15px; border-radius: 20px; font-weight: bold; display: inline-block; margin: 5px 0; }\n");
        html.append("        .status-success { background: #27ae60; color: white; }\n");
        html.append("        .status-error { background: #e74c3c; color: white; }\n");
        html.append("        .status-warning { background: #f39c12; color: white; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
        html.append("        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background: #3498db; color: white; font-weight: bold; }\n");
        html.append("        tr:hover { background: #f8f9fa; }\n");
        html.append("        .error-row { background: #ffebee; }\n");
        html.append("        .warning-row { background: #fff3e0; }\n");
        html.append("        .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #7f8c8d; font-size: 0.9em; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>🔍 API Contract Validation Report</h1>\n");
        html.append("        \n");
        html.append("        <div class=\"summary\">\n");
        html.append("            <h2>📋 Summary</h2>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Base URL:</span>\n");
        html.append("                <span class=\"summary-value\">").append(escapeHtml(result.getBaseUrl())).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Endpoints Checked:</span>\n");
        html.append("                <span class=\"summary-value\">").append(result.getTotalEndpoints()).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Issues Found:</span>\n");
        html.append("                <span class=\"summary-value\">").append(result.getTotalIssues()).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Duration:</span>\n");
        html.append("                <span class=\"summary-value\">").append(result.getDurationMillis()).append(" ms</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Started:</span>\n");
        html.append("                <span class=\"summary-value\">").append(result.getStartedAt().format(TIME_FORMATTER)).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Finished:</span>\n");
        html.append("                <span class=\"summary-value\">").append(result.getFinishedAt().format(TIME_FORMATTER)).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-item\">\n");
        html.append("                <span class=\"summary-label\">Status:</span>\n");
        if (result.hasErrors()) {
            html.append("                <span class=\"status status-error\">❌ FAILED</span>\n");
        } else if (result.getTotalIssues() > 0) {
            html.append("                <span class=\"status status-warning\">⚠️ WARNINGS</span>\n");
        } else {
            html.append("                <span class=\"status status-success\">✅ PASSED</span>\n");
        }
        html.append("            </div>\n");
        html.append("        </div>\n");

        if (result.getTotalIssues() > 0) {
            html.append("        <h2>🚨 Issues Found</h2>\n");
            html.append("        <table>\n");
            html.append("            <thead>\n");
            html.append("                <tr>\n");
            html.append("                    <th>Severity</th>\n");
            html.append("                    <th>Method</th>\n");
            html.append("                    <th>Endpoint</th>\n");
            html.append("                    <th>Description</th>\n");
            html.append("                    <th>Expected</th>\n");
            html.append("                    <th>Actual</th>\n");
            html.append("                </tr>\n");
            html.append("            </thead>\n");
            html.append("            <tbody>\n");

            for (ValidationIssue issue : result.getIssues()) {
                String rowClass = issue.getSeverity() == ValidationIssue.Severity.ERROR ? "error-row" : "warning-row";
                html.append("                <tr class=\"").append(rowClass).append("\">\n");
                html.append("                    <td><span class=\"status status-")
                        .append(issue.getSeverity() == ValidationIssue.Severity.ERROR ? "error" : "warning")
                        .append("\">")
                        .append(issue.getSeverity())
                        .append("</span></td>\n");
                html.append("                    <td>").append(escapeHtml(issue.getMethod())).append("</td>\n");
                html.append("                    <td><code>").append(escapeHtml(issue.getPath())).append("</code></td>\n");
                html.append("                    <td>").append(escapeHtml(issue.getDescription())).append("</td>\n");
                html.append("                    <td>").append(escapeHtml(issue.getExpected() != null ? issue.getExpected() : "-")).append("</td>\n");
                html.append("                    <td>").append(escapeHtml(issue.getActual() != null ? issue.getActual() : "-")).append("</td>\n");
                html.append("                </tr>\n");
            }

            html.append("            </tbody>\n");
            html.append("        </table>\n");
        } else {
            html.append("        <div style=\"text-align: center; padding: 40px; background: #d4edda; border-radius: 5px;\">\n");
            html.append("            <h2 style=\"color: #155724;\">✅ All endpoints passed validation!</h2>\n");
            html.append("            <p style=\"color: #155724; font-size: 1.2em;\">No issues found</p>\n");
            html.append("        </div>\n");
        }

        html.append("        <div class=\"footer\">\n");
        html.append("            <p>Generated by <strong>API Contract Validator</strong> on ")
                .append(result.getFinishedAt().format(TIME_FORMATTER))
                .append("</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}