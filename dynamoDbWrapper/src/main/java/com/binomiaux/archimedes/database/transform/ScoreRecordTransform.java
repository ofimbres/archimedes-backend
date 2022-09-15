package com.binomiaux.archimedes.database.transform;

import com.binomiaux.archimedes.database.schema.ExerciseResultRecord;
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
        student.setFirstName(entity.getStudentName());
        model.setStudent(student);

        return model;
    }

    @Override
    public ExerciseResultRecord untransform(ExerciseResult model) {

        ExerciseResultRecord entity = new ExerciseResultRecord();
        entity.setScore(model.getScore());

        String timestamp = ZonedDateTime.ofInstant(model.getTimestamp(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        entity.setTimestamp(timestamp);
        entity.setStudentName(model.getStudent().getFirstName());

        return entity;
    }
}
