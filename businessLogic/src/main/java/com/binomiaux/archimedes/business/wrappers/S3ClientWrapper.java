package com.binomiaux.archimedes.business.wrappers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class S3ClientWrapper {
    private final AmazonS3 s3Client;
    private static final String BUCKET_NAME = "archimedes-exercise-results";

    public S3ClientWrapper(AmazonS3 client) {
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

        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key, tmpFile);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        request.setMetadata(metadata);
        s3Client.putObject(request);
    }
}
