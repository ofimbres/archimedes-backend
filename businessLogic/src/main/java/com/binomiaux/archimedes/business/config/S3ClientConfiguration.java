package com.binomiaux.archimedes.business.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.binomiaux.archimedes.business.wrappers.S3ClientWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ClientConfiguration {

    @Bean
    public AmazonS3 s3Client() {
        return buildAmazonS3();
    }

    @Bean
    public S3ClientWrapper s3ClientWrapper() {
        return new S3ClientWrapper(buildAmazonS3());
    }

    private AmazonS3 buildAmazonS3() {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.US_WEST_2)
                .build();
    }


}