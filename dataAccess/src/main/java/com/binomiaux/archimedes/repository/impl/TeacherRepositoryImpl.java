package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.api.TeacherRepository;
import com.binomiaux.archimedes.repository.entities.SchoolEntity;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

@Repository
public class TeacherRepositoryImpl implements TeacherRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(Teacher teacher) {
        // Update school entity
        DynamoDbTable<SchoolEntity> schoolTable = enhancedClient.table(tableName, SchoolEntity.TABLE_SCHEMA);
        SchoolEntity schoolEntity = schoolTable.getItem(r -> r.key(k -> k
            .partitionValue("SCHOOL#" + teacher.getSchoolCode())
            .sortValue("#")
        ));
        
        if (schoolEntity == null) {
            throw new IllegalArgumentException("School not found");
        }
        
        int nextTeacherCode = schoolEntity.getTeacherCount() + 1;
        schoolEntity.setTeacherCount(nextTeacherCode);

        teacher.setTeacherCode(String.valueOf(nextTeacherCode));

        // Update teacher entity
        TeacherEntity teacherEntity = new TeacherEntity();
        teacherEntity.setPk("TEACHER#" + teacher.getId());
        teacherEntity.setSk("#");
        teacherEntity.setType("TEACHER");
        teacherEntity.setId(teacher.getId());
        teacherEntity.setSchoolCode(teacher.getSchoolCode());
        teacherEntity.setTeacherCode(teacher.getTeacherCode());
        teacherEntity.setFirstName(teacher.getFirstName());
        teacherEntity.setLastName(teacher.getLastName());
        teacherEntity.setEmail(teacher.getEmail());
        teacherEntity.setUsername(teacher.getUsername());
        teacherEntity.setMaxPeriods(6); // TODO

        DynamoDbTable<TeacherEntity> teacherTable = enhancedClient.table(tableName, TeacherEntity.TABLE_SCHEMA);

        enhancedClient.transactWriteItems(b -> b
            .addPutItem(teacherTable, TransactPutItemEnhancedRequest.builder(TeacherEntity.class)
                .item(teacherEntity)
                .conditionExpression(Expression.builder()
                    .expression("attribute_not_exists(pk)")
                    .build())
                .build())
            .addUpdateItem(schoolTable, schoolEntity)
        );
    }
}