package com.binomiaux.archimedes.repository.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.binomiaux.archimedes.repository.schema.ExerciseRecord;
import com.binomiaux.archimedes.repository.schema.ExerciseResultRecord;
import com.binomiaux.archimedes.repository.schema.StudentRecord;
import com.binomiaux.archimedes.repository.schema.TopicRecord;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
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
    public DynamoDbTable<TopicRecord> topicTable() {
        return buildDynamoDbEnhancedClient().table(tableName, TopicRecord.TABLE_SCHEMA);
    }

    @Bean
    public DynamoDbTable<ExerciseResultRecord> exerciseResultTable() {
        return buildDynamoDbEnhancedClient().table(tableName, ExerciseResultRecord.TABLE_SCHEMA);
    }

    @Bean
    public DynamoDbTable<ExerciseRecord> exerciseTable() {
        return buildDynamoDbEnhancedClient().table(tableName, ExerciseRecord.TABLE_SCHEMA);
    }

    @Bean
    public DynamoDbTable<StudentRecord> studentTable() {
        return buildDynamoDbEnhancedClient().table(tableName, StudentRecord.TABLE_SCHEMA);
    }
}

