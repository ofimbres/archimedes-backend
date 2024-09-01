package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.ExerciseResultRepository;
import com.binomiaux.archimedes.repository.converter.ScoreEntityTransform;
import com.binomiaux.archimedes.repository.entities.ExerciseResultEntity;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.ExerciseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ExerciseResultRepositoryImpl implements ExerciseResultRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private ScoreEntityTransform scoreEntityTransform = new ScoreEntityTransform();

    @Override
    public void create(ExerciseResult score) {
        DynamoDbTable<ExerciseResultEntity> exerciseTable = enhancedClient.table(tableName, ExerciseResultEntity.TABLE_SCHEMA);
        ExerciseResultEntity record = scoreEntityTransform.untransform(score);
        exerciseTable.putItem(record);
    }

    @Override
    public List<ExerciseResult> findByStudentId(String classId, String studentId, String exerciseCode) {
        return null;
    }

    @Override
    public ExerciseResult findByStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode) {
        String pk = "CLASS#" + classId + "#STUDENT#" + studentId + "#EXERCISE#" + exerciseCode;
        DynamoDbTable<ExerciseResultEntity> exerciseTable = enhancedClient.table("dev-archimedes-table", ExerciseResultEntity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).sortValue(pk).build();
        ExerciseResultEntity record = exerciseTable.getItem(r -> r.key(key));
        return scoreEntityTransform.transform(record);
    }

    @Override
    public List<ExerciseResult> findAllByClassIdAndExerciseCode(String classId, String exerciseCode) {
        String pk = "CLASS#" + classId;
        String sk = "EXERCISE#" + exerciseCode;
        DynamoDbTable<ExerciseResultEntity> exerciseTable = enhancedClient.table("dev-archimedes-table", ExerciseResultEntity.TABLE_SCHEMA);
        DynamoDbIndex<ExerciseResultEntity> index = exerciseTable.index("gsi1"); // replace "gsi1" with the name of your index
        Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<ExerciseResultEntity>> pages = index.query(r -> r.queryConditional(queryConditional).limit(10));

        return StreamSupport.stream(pages.spliterator(), false)
                .flatMap(page -> page.items().stream())
                .map(scoreEntityTransform::transform)
                .collect(Collectors.toList());
    }
}
