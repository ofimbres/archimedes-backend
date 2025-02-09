package com.binomiaux.archimedes.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfig {

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        return CognitoIdentityProviderClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_WEST_2)
                .build();
    }
}
