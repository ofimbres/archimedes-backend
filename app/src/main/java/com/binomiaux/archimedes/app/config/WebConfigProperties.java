package com.binomiaux.archimedes.app.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

/**
 * Configuration web properties such as CORS.
 */
@ConfigurationProperties(prefix = "web")
@ConstructorBinding
public class WebConfigProperties {
    @Getter
    private final Cors cors;

    public WebConfigProperties(Cors cors) {
        this.cors = cors;
    }

    /**
     *
     */
    @Getter
    public static class Cors {

        private final List<String> allowedOrigins;

        private final List<String> allowedMethods;

        private final List<String> allowedHeaders;

        private final List<String> exposedHeaders;

        private final long maxAge;

        public Cors(List<String> allowedOrigins, List<String> allowedMethods, long maxAge,
                    List<String> allowedHeaders, List<String> exposedHeaders) {
            this.allowedOrigins = allowedOrigins;
            this.allowedMethods = allowedMethods;
            this.maxAge = maxAge;
            this.allowedHeaders = allowedHeaders;
            this.exposedHeaders = exposedHeaders;
        }
    }
}
