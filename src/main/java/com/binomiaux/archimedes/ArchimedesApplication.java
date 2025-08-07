package com.binomiaux.archimedes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry application.
 */
@SpringBootApplication(scanBasePackages = "com.binomiaux.archimedes")
public class ArchimedesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArchimedesApplication.class, args);
    }
}
