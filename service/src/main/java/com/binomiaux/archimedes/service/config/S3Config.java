package com.binomiaux.archimedes.service.config;

import com.binomiaux.archimedes.service.awsservices.S3Service;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Service s3Service() {
        return new S3Service(s3Client(), s3Presigner());
    }
}