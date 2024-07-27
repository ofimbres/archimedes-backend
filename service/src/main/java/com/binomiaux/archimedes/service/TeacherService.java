package com.binomiaux.archimedes.service;

import java.util.List;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Teacher;

public interface TeacherService {
    void create(Teacher teacher);
    List<Student> getStudentsByPeriod(String periodId);
    List<Period> getPeriodsByTeacher(String teacherId);
}
