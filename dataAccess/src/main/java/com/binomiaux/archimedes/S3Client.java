package com.binomiaux.archimedes;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class S3Client {
    String accessKey = "AKIA5QLPQK5E2GETOS7R";
    String secretKey = "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";

    private final AmazonS3 client;

    public S3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public void upload(String worksheetCopy, String studentId, String exerciseCode, long timestamp) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("test", ".tmp");
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(worksheetCopy);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filename = exerciseCode + "_" + studentId + "_" + timestamp + ".html";
        PutObjectRequest request = new PutObjectRequest("archimedes-exercise-results", filename, tmpFile);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        request.setMetadata(metadata);
        PutObjectResult result = client.putObject(request);
    }
}
