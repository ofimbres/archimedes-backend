package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.ExerciseResultRepository;
import com.binomiaux.archimedes.repository.converter.ExerciseResultEntityConverter;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.dynamodb.ExerciseResultEntity;
import com.binomiaux.archimedes.model.pojo.ExerciseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExerciseResultRepositoryImpl implements ExerciseResultRepository {

    @Autowired
    private DynamoDbTable<ExerciseResultEntity> exerciseResultTable;

    private ExerciseResultEntityConverter transformer = new ExerciseResultEntityConverter();

    @Override
    public void create(ExerciseResult score) {
        exerciseResultTable.putItem(transformer.untransform(score));
    }

    @Override
    public List<ExerciseResult> findByStudentId(String classId, String studentId, String exerciseCode) {
        return null;
    }

    @Override
    public ExerciseResult findByStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode) {
        String pk = "CLASS#" + classId + "#STUDENT#" + studentId + "#EXERCISE#" + exerciseCode;
        ExerciseResultEntity record = exerciseResultTable.getItem(Key.builder().partitionValue(pk).build());

        return transformer.transform(record);
    }

    @Override
    public List<ExerciseResult> findAllByClassIdAndExerciseCode(String classId, String exerciseCode) {
        //String pk = "CLASS#" + classId;
        //String sk = "EXERCISE#" + exerciseCode;

        //ExerciseResultRecord exerciseResult = new ExerciseResultRecord("", "", "", "", "", "", "", 0, "", "", pk, sk);

        /*Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS("EXERCISE#" + exerciseCode.toUpperCase()));

        DynamoDBQueryExpression<ExerciseResultRecord> queryExpression =
                new DynamoDBQueryExpression<ExerciseResultRecord>()
                        .withHashKeyValues(exerciseResult)
                        .withRangeKeyCondition("gsisk", rangeKeyCondition)
                        .withLimit(10)
                        .withIndexName("gsi1")
                        .withConsistentRead(false);*/


        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("EXERCISE#" + exerciseCode.toUpperCase()));
        PageIterable<ExerciseResultEntity> pagedResults = PageIterable.create(exerciseResultTable.index("gsi1").query(query));

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }
}
