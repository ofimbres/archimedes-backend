package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.entities.ExerciseResultEntity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreEntityTransform implements EntityTransform<ExerciseResultEntity, ExerciseResult> {

    @Override
    public ExerciseResult transform(ExerciseResultEntity entity) {
        ExerciseResult model = new ExerciseResult();
        model.setScore(entity.getScore());
        model.setTimestamp(Instant.parse(entity.getTimestamp()));

        Student student = new Student("0", entity.getFirstName(), entity.getLastName(), "", "", "");
        // student.setFirstName(entity.getFirstName());
        // student.setLastName(entity.getLastName());
        model.setStudent(student);
        model.setS3Key(entity.getS3Key());

        return model;
    }

    @Override
    public ExerciseResultEntity untransform(ExerciseResult model) {

        ExerciseResultEntity entity = new ExerciseResultEntity();
        entity.setPk("CLASS#" + model.getClassroom().getPeriodId() + "#STUDENT#" + model.getStudent().getStudentId() + "#EXERCISE#" + model.getExercise().id());
        entity.setSk("CLASS#" + model.getClassroom().getPeriodId() + "#STUDENT#" + model.getStudent().getStudentId() + "#EXERCISE#" + model.getExercise().id());
        //entity.setSk("TIMESTAMP#" + model.getTimestamp());
        entity.setGsipk("CLASS#" + model.getClassroom().getPeriodId());
        entity.setGsisk("EXERCISE#" + model.getExercise().id());
        entity.setType("EXERCISE_RESULT");

        entity.setFirstName(model.getStudent().getFirstName());
        entity.setLastName(model.getStudent().getLastName());
        entity.setExerciseName(model.getExercise().name());

        String timestamp = ZonedDateTime.ofInstant(model.getTimestamp(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        entity.setTimestamp(timestamp);
        entity.setScore(model.getScore());
        entity.setFirstName(model.getStudent().getFirstName());
        entity.setLastName(model.getStudent().getLastName());
        entity.setS3Key(model.getS3Key());

        return entity;
    }
}
