package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.api.TeacherRepository;
import com.binomiaux.archimedes.repository.entities.SchoolEntity;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;
import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;
import com.binomiaux.archimedes.repository.mapper.TeacherMapper;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

@Repository
public class TeacherRepositoryImpl implements TeacherRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private TeacherMapper mapper = TeacherMapper.INSTANCE;

    @Override
    public Teacher find(String teacherId) {
        String pk = "TEACHER#" + teacherId;
        DynamoDbTable<TeacherEntity> studentTable = enhancedClient.table(tableName, TeacherEntity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).sortValue("#METADATA").build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        TeacherEntity record = studentTable.getItem(request);

        return mapper.entityToTeacher(record);
    }

    @Override
    public void create(Teacher teacher) {
        // Update school entity
        DynamoDbTable<SchoolEntity> schoolTable = enhancedClient.table(tableName, SchoolEntity.TABLE_SCHEMA);
        SchoolEntity schoolEntity = schoolTable.getItem(r -> r.key(k -> k
            .partitionValue("SCHOOL#" + teacher.getSchoolId())
            .sortValue("#METADATA")
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
        teacherEntity.setPk("TEACHER#" + teacher.getTeacherId());
        teacherEntity.setSk("#METADATA");
        teacherEntity.setType("TEACHER");
        teacherEntity.setSchoolId(teacher.getSchoolId());
        teacherEntity.setTeacherId(teacher.getTeacherId());
        teacherEntity.setFirstName(teacher.getFirstName());
        teacherEntity.setLastName(teacher.getLastName());
        teacherEntity.setEmail(teacher.getEmail());
        teacherEntity.setUsername(teacher.getUsername());
        teacherEntity.setMaxPeriods(6); // TODO
        teacherEntity.setGsi1pk("SCHOOL#" + teacher.getSchoolId());
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