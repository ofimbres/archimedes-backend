package com.binomiaux.archimedes.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;

/**
 * Configuration web properties such as CORS.
 */
@ConfigurationProperties(prefix = "web.cors")
public class Cors {
    private final List<String> allowedOrigins;
    private final List<String> allowedMethods;
    private final List<String> allowedHeaders;
    private final List<String> exposedHeaders;
    private final long maxAge;

    @ConstructorBinding
    public Cors(List<String> allowedOrigins, List<String> allowedMethods, long maxAge,
                                List<String> allowedHeaders, List<String> exposedHeaders) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.maxAge = maxAge;
        this.allowedHeaders = allowedHeaders;
        this.exposedHeaders = exposedHeaders;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public long getMaxAge() {
        return maxAge;
    }
}
