package com.binomiaux.archimedes.database.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.binomiaux.archimedes.database.repository.ExerciseResultRepository;
import com.binomiaux.archimedes.database.schema.ExerciseResultRecord;
import com.binomiaux.archimedes.database.transform.ScoreRecordTransform;
import com.binomiaux.archimedes.model.ExerciseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExerciseResultRepositoryImpl implements ExerciseResultRepository {

    @Autowired
    private DynamoDBMapper mapper;

    private ScoreRecordTransform scoreRecordTransform = new ScoreRecordTransform();

    @Override
    public void create(ExerciseResult score) {
        mapper.save(scoreRecordTransform.untransform(score));
    }

    @Override
    public ExerciseResult findByClassIdStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode) {
        String pk = "CLASS#" + classId + "#STUDENT#" + studentId + "#EXERCISE#" + exerciseCode;
        ExerciseResultRecord record = mapper.load(ExerciseResultRecord.class, pk, pk);

        return scoreRecordTransform.transform(record);
    }

    @Override
    public List<ExerciseResult> findAllByClassIdAndExerciseCode(String classId, String exerciseCode) {
        ExerciseResultRecord exerciseResult = new ExerciseResultRecord();
        exerciseResult.setGsipk("CLASS#" + classId);
        exerciseResult.setGsisk("EXERCISE#" + exerciseCode);

        Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS("EXERCISE#" + exerciseCode.toUpperCase()));

        DynamoDBQueryExpression<ExerciseResultRecord> queryExpression =
                new DynamoDBQueryExpression<ExerciseResultRecord>()
                        .withHashKeyValues(exerciseResult)
                        .withRangeKeyCondition("gsisk", rangeKeyCondition)
                        .withLimit(10)
                        .withIndexName("gsi1")
                        .withConsistentRead(false);

        List<ExerciseResultRecord> queryResult = mapper.query(ExerciseResultRecord.class, queryExpression);
        return queryResult.stream().map(r -> scoreRecordTransform.transform(r)).collect(Collectors.toList());
    }
}
