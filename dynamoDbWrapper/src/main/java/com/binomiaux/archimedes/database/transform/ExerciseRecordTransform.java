package com.binomiaux.archimedes.database.transform;

import com.binomiaux.archimedes.database.schema.ExerciseRecord;
import com.binomiaux.archimedes.model.Exercise;

public class ExerciseRecordTransform  implements RecordTransform<ExerciseRecord, Exercise> {
    @Override
    public Exercise transform(ExerciseRecord entity) {
        Exercise model = new Exercise();

        return model;
    }

    @Override
    public ExerciseRecord untransform(Exercise model) {
        return null;
    }
}
