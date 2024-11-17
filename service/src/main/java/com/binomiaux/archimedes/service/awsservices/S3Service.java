package com.binomiaux.archimedes.service.awsservices;

import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;

public class S3Service {
    private final S3Client s3Client;

    @Value("${s3.exercise-results-bucket-name}")
    private String exerciseResultsBucketName;

    public S3Service(S3Client client) {
        s3Client = client;
    }

    public void uploadWorksheet(String key, String fileContent) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(key, ".tmp");
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(fileContent);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key, tmpFile);
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setContentType("text/html");
        // request.setMetadata(metadata);
        // s3Client.putObject(request);
    }
}
