package com.binomiaux.archimedes.repository.transform;

import com.binomiaux.archimedes.repository.schema.ExerciseResultRecord;
import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.Student;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreRecordTransform implements RecordTransform<ExerciseResultRecord, ExerciseResult> {

    @Override
    public ExerciseResult transform(ExerciseResultRecord entity) {
        ExerciseResult model = new ExerciseResult();
        model.setScore(entity.getScore());
        model.setTimestamp(Instant.parse(entity.getTimestamp()));

        Student student = new Student();
        student.setFirstName(entity.getFirstName());
        student.setLastName(entity.getLastName());
        model.setStudent(student);
        model.setS3Key(entity.getS3Key());

        return model;
    }

    @Override
    public ExerciseResultRecord untransform(ExerciseResult model) {

        ExerciseResultRecord entity = new ExerciseResultRecord();
        entity.setPk("CLASS#" + model.getClassroom().getId() + "#STUDENT#" + model.getStudent().getId() + "#EXERCISE#" + model.getExercise().id);
        entity.setSk("CLASS#" + model.getClassroom().getId() + "#STUDENT#" + model.getStudent().getId() + "#EXERCISE#" + model.getExercise().id);
        //entity.setSk("TIMESTAMP#" + model.getTimestamp());
        entity.setGsipk("CLASS#" + model.getClassroom().getId());
        entity.setGsisk("EXERCISE#" + model.getExercise().getId());
        entity.setType("EXERCISE_RESULT");

        entity.setFirstName(model.getStudent().getFirstName());
        entity.setLastName(model.getStudent().getLastName());
        entity.setExerciseName(model.getExercise().getName());

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
