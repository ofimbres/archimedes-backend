package com.binomiaux.archimedes.repository.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.binomiaux.archimedes.model.dynamodb.ExerciseEntity;
import com.binomiaux.archimedes.model.dynamodb.ExerciseResultEntity;
import com.binomiaux.archimedes.model.dynamodb.StudentEntity;
import com.binomiaux.archimedes.model.dynamodb.TopicEntity;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDbConfig {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private AWSCredentialsProvider awsCredentials() {
        return new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(accessKey, secretKey));
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(buildAmazonDynamoDB());
    }

    private AmazonDynamoDB buildAmazonDynamoDB() {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(awsCredentials())
                .withRegion(Regions.US_WEST_2)
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient buildDynamoDbEnhancedClient() {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(
                            // Configure an instance of the standard client.
                            DynamoDbClient.builder()
                                    .region(software.amazon.awssdk.regions.Region.US_WEST_2)
                                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                                    .build())
                    .build();
        return enhancedClient;
    }

    @Bean
    public DynamoDbTable<TopicEntity> topicTable() {
        return buildDynamoDbEnhancedClient().table(tableName, TopicEntity.TABLE_SCHEMA);
    }

    @Bean
    public DynamoDbTable<ExerciseResultEntity> exerciseResultTable() {
        return buildDynamoDbEnhancedClient().table(tableName, ExerciseResultEntity.TABLE_SCHEMA);
    }

    @Bean
    public DynamoDbTable<ExerciseEntity> exerciseTable() {
        return buildDynamoDbEnhancedClient().table(tableName, ExerciseEntity.TABLE_SCHEMA);
    }

    @Bean
    public DynamoDbTable<StudentEntity> studentTable() {
        return buildDynamoDbEnhancedClient().table(tableName, StudentEntity.TABLE_SCHEMA);
    }
}

