package com.binomiaux.archimedes.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 *
 */
@ConfigurationProperties(prefix = "web")
@ConstructorBinding
public class WebConfigProperties {

    private final Cors cors;

    public WebConfigProperties(Cors cors) {
        this.cors = cors;
    }

    public Cors getCors() {
        return cors;
    }

    /**
     *
     */
    @Data
    public static class Cors {

        private final String[] allowedOrigins;

        private final String[] allowedMethods;

        private final String[] allowedHeaders;

        private final String[] exposedHeaders;

        private final long maxAge;

        public Cors(String[] allowedOrigins, String[] allowedMethods, long maxAge,
                    String[] allowedHeaders, String[] exposedHeaders) {
            this.allowedOrigins = allowedOrigins;
            this.allowedMethods = allowedMethods;
            this.maxAge = maxAge;
            this.allowedHeaders = allowedHeaders;
            this.exposedHeaders = exposedHeaders;
        }
    }
}
