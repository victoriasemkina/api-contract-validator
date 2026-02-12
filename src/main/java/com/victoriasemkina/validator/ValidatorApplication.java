package com.victoriasemkina.validator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;
import com.victoriasemkina.validator.cli.command.ValidatorCommand;

/**
 * Main application entry point.
 * Starts Spring context and delegates to Picocli command.
 */
@SpringBootApplication  // ← Добавляем эту аннотацию для сканирования компонентов
public class ValidatorApplication {

    public static void main(String[] args) {
        // Start minimal Spring context (no web server)
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ValidatorApplication.class)
                .web(WebApplicationType.NONE)
                .logStartupInfo(false)
                .run(args);

        // Get command bean from Spring context (with injected dependencies)
        ValidatorCommand command = context.getBean(ValidatorCommand.class);

        // Execute CLI command
        int exitCode = new CommandLine(command).execute(args);
        System.exit(exitCode);
    }
}