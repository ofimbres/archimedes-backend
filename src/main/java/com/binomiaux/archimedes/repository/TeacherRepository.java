package com.binomiaux.archimedes.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

/*
 * Simplified TeacherRepository without interface abstraction.
 * Direct implementation reduces complexity for single-implementation repositories.
 */
@Repository
public class TeacherRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;


    public Teacher find(String teacherId) {
        DynamoDbTable<Teacher> teacherTable = enhancedClient.table(dynamoDbProperties.getTableName(), Teacher.TABLE_SCHEMA);
        Key key = Key.builder()
            .partitionValue(DynamoKeyBuilder.buildTeacherKey(teacherId))
            .sortValue(DynamoKeyBuilder.METADATA_KEY)
            .build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        Teacher record = teacherTable.getItem(request);

        return record;
    }

    public void create(Teacher teacher) {
        // Get school entity to update counter
        DynamoDbTable<School> schoolTable = enhancedClient.table(dynamoDbProperties.getTableName(), School.TABLE_SCHEMA);
        School schoolEntity = schoolTable.getItem(r -> r.key(k -> k
            .partitionValue(DynamoKeyBuilder.buildSchoolKey(teacher.getSchoolId()))
            .sortValue(DynamoKeyBuilder.METADATA_KEY)
        ));
        
        // Generate teacher ID
        int nextTeacherCode = schoolEntity.getTeacherCount() + 1;
        schoolEntity.setTeacherCount(nextTeacherCode);
        teacher.setTeacherId(teacher.getSchoolId() + "-T" + String.valueOf(nextTeacherCode));

        // Create teacher entity with simplified key generation
        Teacher teacherEntity = new Teacher();
        teacherEntity.setPk(DynamoKeyBuilder.buildTeacherKey(teacher.getTeacherId()));
        teacherEntity.setSk(DynamoKeyBuilder.METADATA_KEY);
        teacherEntity.setEntityType("TEACHER");
        teacherEntity.setSchoolId(teacher.getSchoolId());
        teacherEntity.setTeacherId(teacher.getTeacherId());
        teacherEntity.setFirstName(teacher.getFirstName());
        teacherEntity.setLastName(teacher.getLastName());
        teacherEntity.setEmail(teacher.getEmail());
        teacherEntity.setUsername(teacher.getUsername());
        teacherEntity.setMaxPeriods(dynamoDbProperties.getDefaultMaxPeriods());
        teacherEntity.setParentEntityKey(DynamoKeyBuilder.buildSchoolKey(teacher.getSchoolId()));
        teacherEntity.setChildEntityKey(DynamoKeyBuilder.buildTeacherKey(teacher.getTeacherId()));
        teacherEntity.setSearchTypeKey("EMAIL");
        teacherEntity.setSearchValueKey(teacher.getEmail());

        DynamoDbTable<Teacher> teacherTable = enhancedClient.table(dynamoDbProperties.getTableName(), Teacher.TABLE_SCHEMA);

        enhancedClient.transactWriteItems(b -> b
            .addPutItem(teacherTable, TransactPutItemEnhancedRequest.builder(Teacher.class)
                .item(teacherEntity)
                .build())
            .addUpdateItem(schoolTable, schoolEntity)
        );
    }
}