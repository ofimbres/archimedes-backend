package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.dynamodb.ExerciseResultEntity;
import com.binomiaux.archimedes.model.pojo.ExerciseResult;
import com.binomiaux.archimedes.model.pojo.Student;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExerciseResultEntityConverter implements EntityConverter<ExerciseResultEntity, ExerciseResult> {

    @Override
    public ExerciseResult transform(ExerciseResultEntity entity) {
        Student student = new Student("0", entity.getFirstName(), entity.getLastName(), "");
        // student.setFirstName(entity.getFirstName());
        // student.setLastName(entity.getLastName());

        ExerciseResult model = new ExerciseResult(null, student, null, entity.getScore(), Instant.parse(entity.getTimestamp()), null, entity.getS3Key());
        return model;
    }

    @Override
    public ExerciseResultEntity untransform(ExerciseResult model) {
        String pk = "CLASS#" + model.classroom().id() + "#STUDENT#" + model.student().id() + "#EXERCISE#" + model.exercise().id();
        String sk = "CLASS#" + model.classroom().id() + "#STUDENT#" + model.student().id() + "#EXERCISE#" + model.exercise().id();

        String timestamp = ZonedDateTime.ofInstant(model.timestamp(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        String gsipk = "CLASS#" + model.classroom().id();
        String gsisk = "EXERCISE#" + model.exercise().id();

        ExerciseResultEntity entity = new ExerciseResultEntity(pk, sk,
            model.student().firstName(),
            model.student().lastName(),
            "",
            model.exercise().name(),
            timestamp,
            model.score(),
            model.s3Key(),
            "EXERCISE_RESULT",
            gsipk,
            gsisk
        );

        return entity;
    }

    /*@Override
    public ExerciseResult transform(ExerciseResultRecord entity) {
        ExerciseResult model = new ExerciseResult();
        model.setScore(entity.getScore());
        model.setTimestamp(Instant.parse(entity.getTimestamp()));

        Student student = new Student("0", entity.getFirstName(), entity.getLastName(), "");
        // student.setFirstName(entity.getFirstName());
        // student.setLastName(entity.getLastName());
        model.setStudent(student);
        model.setS3Key(entity.getS3Key());

        return model;
    }

    @Override
    public ExerciseResultRecord untransform(ExerciseResult model) {

        ExerciseResultRecord entity = new ExerciseResultRecord();
        entity.setPk("CLASS#" + model.getClassroom().id() + "#STUDENT#" + model.getStudent().id() + "#EXERCISE#" + model.getExercise().id());
        entity.setSk("CLASS#" + model.getClassroom().id() + "#STUDENT#" + model.getStudent().id() + "#EXERCISE#" + model.getExercise().id());
        //entity.setSk("TIMESTAMP#" + model.getTimestamp());
        entity.setGsipk("CLASS#" + model.getClassroom().id());
        entity.setGsisk("EXERCISE#" + model.getExercise().id());
        entity.setType("EXERCISE_RESULT");

        entity.setFirstName(model.getStudent().firstName());
        entity.setLastName(model.getStudent().lastName());
        entity.setExerciseName(model.getExercise().name());

        String timestamp = ZonedDateTime.ofInstant(model.getTimestamp(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        entity.setTimestamp(timestamp);
        entity.setScore(model.getScore());
        entity.setFirstName(model.getStudent().firstName());
        entity.setLastName(model.getStudent().lastName());
        entity.setS3Key(model.getS3Key());

        return entity;
    }*/
}
