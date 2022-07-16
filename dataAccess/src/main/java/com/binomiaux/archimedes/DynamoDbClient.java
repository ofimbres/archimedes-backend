package com.binomiaux.archimedes;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public class DynamoDbClient {
    String accessKey = "AKIA5QLPQK5E2GETOS7R";
    String secretKey = "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";

    private AmazonDynamoDB client;
    private DynamoDBMapper mapper;

    public DynamoDbClient() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        mapper = new DynamoDBMapper(client);
    }

    public ExerciseResults getLatestExerciseResults(String studentId, String exerciseCode) {
        ExerciseResults item = new ExerciseResults();
        item.setPk("STUDENT#" + studentId + "EXERCISE#" + exerciseCode);
        DynamoDBQueryExpression<ExerciseResults> query =
                new DynamoDBQueryExpression()
                .withHashKeyValues(item)
                .withScanIndexForward(false)
                .withLimit(1);
        QueryResultPage<ExerciseResults> results = mapper.queryPage(ExerciseResults.class, query);
        return results.getResults().get(0);
    }

    public void setExerciseResults(String studentCode, String exerciseCode, int score, long timestamp) {
        ExerciseResults item = new ExerciseResults();
        item.setPk("STUDENT#" + studentCode + "EXERCISE#" + exerciseCode);
        item.setCreatedAt(timestamp);
        item.setScore(score);

        mapper.save(item);
    }
}
