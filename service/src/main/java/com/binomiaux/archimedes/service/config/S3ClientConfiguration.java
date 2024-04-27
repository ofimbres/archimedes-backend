package com.binomiaux.archimedes.service.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.binomiaux.archimedes.service.wrappers.S3ClientWrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ClientConfiguration {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    private AWSCredentialsProvider awsCredentials() {
        return new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(accessKey, secretKey));
    }

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
                .withCredentials(awsCredentials())
                .withRegion(Regions.US_WEST_2)
                .build();
    }
}