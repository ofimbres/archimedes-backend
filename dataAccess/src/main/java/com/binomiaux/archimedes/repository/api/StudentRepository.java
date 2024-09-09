package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.Student;
import java.util.List;

public interface StudentRepository {
    Student find(String studentId);
    void create(Student student);
    void enrollInPeriod(String studentId, String periodId);
    void unrollInPeriod(String studentId, String periodId);
    List<Student> getStudentsByPeriod(String periodId);
}
