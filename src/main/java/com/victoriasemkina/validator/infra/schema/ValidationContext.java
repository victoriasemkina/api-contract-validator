package com.victoriasemkina.validator.infra.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Context object passed through validation chain
 * Contains information about current validation state
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationContext {

    /** Current JSON path (e.g., "$.users[0].name") */
    private String fieldPath;

    /** API endpoint path (e.g., "/users") */
    private String endpointPath;

    /** HTTP method (e.g., "GET") */
    private String httpMethod;

    public ValidationContext withFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
        return this;
    }

    public ValidationContext withEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
        return this;
    }

    public ValidationContext withHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }
}