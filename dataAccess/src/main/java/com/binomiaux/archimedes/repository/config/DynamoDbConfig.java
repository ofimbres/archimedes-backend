package com.binomiaux.archimedes.repository.config;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        DynamoDbClient standardClient = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_WEST_2)
                .build();

        return standardClient;
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        DynamoDbClient standardClient = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_WEST_2)
                .build();
        
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standardClient)
            .build();
    }
}

