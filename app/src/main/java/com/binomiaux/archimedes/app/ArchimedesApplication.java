package com.binomiaux.archimedes.app;

import com.binomiaux.archimedes.repository.ArchimedesRepositoryScan;
import com.binomiaux.archimedes.service.ArchimedesServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * The entry application.
 */
@SpringBootApplication
@Import(value = { ArchimedesServiceScan.class, ArchimedesRepositoryScan.class })
public class ArchimedesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArchimedesApplication.class, args);
    }
}
