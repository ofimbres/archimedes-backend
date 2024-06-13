package com.binomiaux.archimedes.repository.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDbConfig {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));

        DynamoDbClient standardClient = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_WEST_2)
                .build();
        
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standardClient)
            .build();
    }
}

