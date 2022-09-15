package com.binomiaux.archimedes.database.dao.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.binomiaux.archimedes.database.dao.ExerciseResultDao;
import com.binomiaux.archimedes.database.schema.ExerciseResultRecord;
import com.binomiaux.archimedes.database.transform.ScoreRecordTransform;
import com.binomiaux.archimedes.model.ExerciseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExerciseResultDaoImpl implements ExerciseResultDao {

    @Autowired
    private DynamoDBMapper mapper;

    private ScoreRecordTransform scoreRecordTransform = new ScoreRecordTransform();

    @Override
    public void create(ExerciseResult score) {
        mapper.save(scoreRecordTransform.untransform(score));
    }

    @Override
    public ExerciseResult getByClassIdStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode) {
        return null;
    }

    @Override
    public List<ExerciseResult> getByClassIdAndExerciseCode(String classId, String exerciseCode) {
        ExerciseResultRecord exerciseResult = new ExerciseResultRecord();
        exerciseResult.setPk("CLASS#" + classId.toUpperCase());
        exerciseResult.setGsipk("CLASS#" + classId.toUpperCase());
        exerciseResult.setGsisk("EXERCISE#" + exerciseCode.toUpperCase());

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
        //List<ExerciseResultRecord> queryResult = mapper.batchLoad(exerciseResult);
        List<ExerciseResultRecord> queryResult = mapper.query(ExerciseResultRecord.class, queryExpression);
        return queryResult.stream().map(r -> scoreRecordTransform.transform(r)).collect(Collectors.toList());
    }

    // https://stackoverflow.com/questions/55455044/how-to-convert-a-pojo-to-mapstring-attributevalue-for-dynamo-db
    /*private ScoreRecord transform(Score object) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Item item = Item.fromJSON(jsonStr);

        Map<String, AttributeValue> valueMap = ItemUtils.toAttributeValues(item);
        ScoreRecord entity = mapper.marshallIntoObject(ScoreRecord.class, valueMap);
        return entity;
    }

    // https://stackoverflow.com/questions/4486787/jackson-with-json-unrecognized-field-not-marked-as-ignorable
    private Score transform(ScoreRecord object) {
        ObjectMapper objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        HashMap<String, Object> hashMap = objectMapper.convertValue(object, HashMap.class);
        return objectMapper.convertValue(hashMap, Score.class);
    }*/
}
