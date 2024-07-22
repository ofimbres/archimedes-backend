package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.api.TeacherRepository;
import com.binomiaux.archimedes.repository.entities.SchoolEntity;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;
import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;

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
            .partitionValue("SCHOOL#" + teacher.getSchoolId())
            .sortValue("#")
        ));
        
        // TODO Move to service layer
        if (schoolEntity == null) {
            throw new EntityNotFoundException("School " + teacher.getSchoolId() + " not found", null);
        }
        
        int nextTeacherCode = schoolEntity.getTeacherCount() + 1;
        schoolEntity.setTeacherCount(nextTeacherCode);

        teacher.setTeacherId(teacher.getSchoolId() + "-T" + String.valueOf(nextTeacherCode));

        // Update teacher entity
        TeacherEntity teacherEntity = new TeacherEntity();
        teacherEntity.setPk("SCHOOL#" + teacher.getSchoolId());
        teacherEntity.setSk("TEACHER#" + teacher.getTeacherId());
        teacherEntity.setType("TEACHER");
        teacherEntity.setSchoolId(teacher.getSchoolId());
        teacherEntity.setTeacherId(teacher.getTeacherId());
        teacherEntity.setFirstName(teacher.getFirstName());
        teacherEntity.setLastName(teacher.getLastName());
        teacherEntity.setEmail(teacher.getEmail());
        teacherEntity.setUsername(teacher.getUsername());
        teacherEntity.setMaxPeriods(6); // TODO
        teacherEntity.setGsi1pk("TEACHER#" + teacher.getTeacherId());
        teacherEntity.setGsi1sk("TEACHER#" + teacher.getTeacherId());
        teacherEntity.setGsi2pk("SCHOOL#" + teacher.getSchoolId());
        teacherEntity.setGsi2sk("TEACHER#" + teacher.getFirstName() + " " + teacher.getLastName());

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