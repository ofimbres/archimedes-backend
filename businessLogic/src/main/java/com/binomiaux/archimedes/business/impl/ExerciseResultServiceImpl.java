package com.binomiaux.archimedes.business.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.binomiaux.archimedes.business.ExerciseResultService;
import com.binomiaux.archimedes.database.dao.ExerciseResultDao;
import com.binomiaux.archimedes.model.ExerciseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ExerciseResultServiceImpl implements ExerciseResultService {
    @Autowired
    private ExerciseResultDao exerciseResultDao;
    @Autowired
    private AmazonS3 s3Client;

    @Override
    public void create(ExerciseResult exerciseResult) {
        exerciseResultDao.create(exerciseResult);

        String key = exerciseResult.getExercise().getId() + "_" + exerciseResult.getStudent().getId() + "_" + exerciseResult.getTimestamp().getEpochSecond();
        storeAsHtml(key, exerciseResult.getWorksheetContent());
    }

    @Override
    public List<ExerciseResult> getByClassAndExercise(String className, String exerciseCode) {
        return exerciseResultDao.getByClassIdAndExerciseCode(className, exerciseCode);
    }

    String BUCKET_NAME = "archimedes-exercise-results";
    private void storeAsHtml(String key, String fileContent) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(key, ".tmp");
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(fileContent);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key + ".html", tmpFile);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        request.setMetadata(metadata);
        s3Client.putObject(request);
    }
}
