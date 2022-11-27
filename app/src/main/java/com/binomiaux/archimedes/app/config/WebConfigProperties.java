package com.binomiaux.archimedes.app.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

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
