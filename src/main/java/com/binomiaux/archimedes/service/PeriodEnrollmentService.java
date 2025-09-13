package com.binomiaux.archimedes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.EnrollmentRepository;
import com.binomiaux.archimedes.repository.PeriodRepository;
import com.binomiaux.archimedes.repository.StudentRepository;

@Service
public class PeriodEnrollmentService {

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    /**
     * Simplified enrollment method using clean IDs.
     * @param schoolId School identifier (e.g., "SCH001")
     * @param studentId Simple student ID (e.g., "S001") 
     * @param periodId Simple period ID (e.g., "P001")
     * @return Created enrollment
     */
    public Enrollment enrollStudent(String schoolId, String studentId, String periodId) {
        // Direct enrollment with simplified school-scoped period lookup
        return enrollStudentInternal(schoolId, studentId, periodId);
    }

    /**
     * Simplified unenrollment method using clean IDs.
     * @param schoolId School identifier (e.g., "SCH001")
     * @param studentId Simple student ID (e.g., "S001")
     * @param periodId Simple period ID (e.g., "P001")
     */
    public void unenrollStudent(String schoolId, String studentId, String periodId) {
        // Direct unenrollment with simplified school-scoped period lookup
        unenrollStudentInternal(schoolId, studentId, periodId);
    }

    /**
     * Simplified method to check if student is enrolled using clean IDs.
     * @param schoolId School identifier (e.g., "SCH001")
     * @param studentId Simple student ID (e.g., "S001")
     * @param periodId Simple period ID (e.g., "P001")
     * @return true if student is enrolled in the period
     */
    public boolean isStudentEnrolled(String schoolId, String studentId, String periodId) {
        // Direct check with simplified school-scoped period lookup
        return isStudentEnrolledInternal(schoolId, studentId, periodId);
    }

    // Internal methods (simplified for school-scoped periods)
    public Enrollment enrollStudentInternal(String schoolId, String studentId, String periodId) {
        Student student = studentRepository.find(schoolId, studentId);
        Period period = periodRepository.find(schoolId, periodId);

        if (student == null) {
            throw new EntityNotFoundException("Student " + studentId + " not found", null);
        }

        if (period == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found in school " + schoolId, null);
        }

        // Generate enrollment ID
        String enrollmentId = idGeneratorService.generateEnrollmentId(schoolId);

        // Create enrollment with simplified structure
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setStudentId(student.getStudentId());
        enrollment.setPeriodId(period.getPeriodId());
        enrollment.setSchoolId(schoolId); // Set the school ID for proper key generation
        enrollment.setStudentFirstName(student.getFirstName());
        enrollment.setStudentLastName(student.getLastName());
        enrollment.setStudentFullName(student.getFirstName() + " " + student.getLastName());
        enrollment.setPeriodName(period.getName());
        enrollment.setPeriodNumber(String.valueOf(period.getPeriodNumber()));
        enrollment.setPeriodDisplayName(period.getName() + " (Period " + period.getPeriodNumber() + ")");
        enrollment.setTeacherLastName(period.getTeacherLastName());
        enrollment.setEnrollmentDate(java.time.LocalDate.now().toString());
        enrollment.setStatus("ACTIVE");
        enrollment.setEntityType("ENROLLMENT");
        
        // Generate DynamoDB keys
        enrollment.generateKeys();

        enrollmentRepository.create(enrollment);
        return enrollment;
    }

    public void unenrollStudentInternal(String schoolId, String studentId, String periodId) {
        enrollmentRepository.delete(studentId, periodId);
    }

    public boolean isStudentEnrolledInternal(String schoolId, String studentId, String periodId) {
        // Check if student is already enrolled by getting their enrollments
        return enrollmentRepository.getEnrollmentsByStudent(schoolId, studentId)
            .stream()
            .anyMatch(enrollment -> enrollment.getPeriodId().equals(periodId));
    }
}
