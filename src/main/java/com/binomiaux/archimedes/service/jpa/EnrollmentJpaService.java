package com.binomiaux.archimedes.service.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.Enrollment;
import com.binomiaux.archimedes.entity.Period;
import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Student;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.repository.jpa.EnrollmentRepository;
import com.binomiaux.archimedes.repository.jpa.PeriodRepository;
import com.binomiaux.archimedes.repository.jpa.SchoolRepository;
import com.binomiaux.archimedes.repository.jpa.StudentRepository;

/**
 * Pure JPA-based EnrollmentService that uses only JPA entities and repositories.
 * No dependencies on DynamoDB services or models.
 */
@Service
@Transactional
public class EnrollmentJpaService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final PeriodRepository periodRepository;
    private final SchoolRepository schoolRepository;

    public EnrollmentJpaService(EnrollmentRepository enrollmentRepository, 
                               StudentRepository studentRepository, 
                               PeriodRepository periodRepository,
                               SchoolRepository schoolRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.periodRepository = periodRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * Get an enrollment by its enrollment ID (business identifier)
     */
    public Enrollment getEnrollmentByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findByEnrollmentId(enrollmentId)
            .orElseThrow(() -> new EntityNotFoundException("Enrollment not found: " + enrollmentId, null));
    }

    /**
     * Get an enrollment by its database ID
     */
    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with ID: " + id, null));
    }

    /**
     * Get all enrollments
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    /**
     * Get enrollments by student ID
     */
    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        Student student = getStudentByStudentId(studentId);
        return enrollmentRepository.findByStudent(student);
    }

    /**
     * Get active enrollments by student ID
     */
    public List<Enrollment> getActiveEnrollmentsByStudentId(String studentId) {
        Student student = getStudentByStudentId(studentId);
        return enrollmentRepository.findByStudentAndStatus(student, "ACTIVE");
    }

    /**
     * Get enrollments by period ID
     */
    public List<Enrollment> getEnrollmentsByPeriodId(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        return enrollmentRepository.findByPeriod(period);
    }

    /**
     * Get active enrollments by period ID ordered by student name
     */
    public List<Enrollment> getActiveEnrollmentsByPeriodIdOrderedByStudent(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        return enrollmentRepository.findActiveEnrollmentsByPeriodOrderedByStudent(period.getId());
    }

    /**
     * Get active enrollments by student ID ordered by period
     */
    public List<Enrollment> getActiveEnrollmentsByStudentIdOrderedByPeriod(String studentId) {
        Student student = getStudentByStudentId(studentId);
        return enrollmentRepository.findActiveEnrollmentsByStudentOrderedByPeriod(student.getId());
    }

    /**
     * Get active enrollments by school code
     */
    public List<Enrollment> getActiveEnrollmentsBySchoolCode(String schoolCode) {
        // We need to get the school first, but we can work with the school ID from any student
        // For now, let's use a query parameter approach
        return enrollmentRepository.findAll().stream()
            .filter(e -> e.getStudent().getSchool().getSchoolCode().equals(schoolCode))
            .filter(e -> "ACTIVE".equals(e.getStatus()))
            .toList();
    }

    /**
     * Enroll a student in a period
     */
    public Enrollment enrollStudent(String studentId, String periodId) {
        Student student = getStudentByStudentId(studentId);
        Period period = getPeriodByPeriodId(periodId);
        
        // Check if student is already enrolled
        if (enrollmentRepository.existsByStudentAndPeriodAndStatus(student, period, "ACTIVE")) {
            throw new IllegalArgumentException("Student is already enrolled in this period");
        }
        
        // Check period capacity
        List<Enrollment> activeEnrollments = enrollmentRepository.findByPeriodAndStatus(period, "ACTIVE");
        if (period.getMaxStudents() != null && activeEnrollments.size() >= period.getMaxStudents()) {
            throw new IllegalArgumentException("Period has reached maximum capacity");
        }
        
        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(generateEnrollmentId());
        enrollment.setStudent(student);
        enrollment.setPeriod(period);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("ACTIVE");
        enrollment.setCreatedDate(LocalDate.now());
        
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Unenroll a student from a period (soft delete)
     */
    public void unenrollStudent(String studentId, String periodId) {
        Student student = getStudentByStudentId(studentId);
        Period period = getPeriodByPeriodId(periodId);
        
        Enrollment enrollment = enrollmentRepository.findByStudentAndPeriod(student, period)
            .orElseThrow(() -> new EntityNotFoundException("Enrollment not found for student " + studentId + " in period " + periodId, null));
        
        enrollment.setStatus("INACTIVE");
        enrollmentRepository.save(enrollment);
    }

    /**
     * Delete an enrollment permanently
     */
    public void deleteEnrollment(String enrollmentId) {
        Enrollment enrollment = getEnrollmentByEnrollmentId(enrollmentId);
        enrollmentRepository.delete(enrollment);
    }

    /**
     * Update enrollment grade
     */
    public Enrollment updateEnrollmentGrade(String enrollmentId, String grade) {
        Enrollment enrollment = getEnrollmentByEnrollmentId(enrollmentId);
        enrollment.setGrade(grade);
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Reactivate an enrollment
     */
    public Enrollment reactivateEnrollment(String enrollmentId) {
        Enrollment enrollment = getEnrollmentByEnrollmentId(enrollmentId);
        enrollment.setStatus("ACTIVE");
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Check if a student is enrolled in a period
     */
    public boolean isStudentEnrolledInPeriod(String studentId, String periodId) {
        Student student = getStudentByStudentId(studentId);
        Period period = getPeriodByPeriodId(periodId);
        return enrollmentRepository.existsByStudentAndPeriodAndStatus(student, period, "ACTIVE");
    }

    /**
     * Get enrollment between specific student and period
     */
    public Enrollment getEnrollmentByStudentAndPeriod(String studentId, String periodId) {
        Student student = getStudentByStudentId(studentId);
        Period period = getPeriodByPeriodId(periodId);
        return enrollmentRepository.findByStudentAndPeriod(student, period)
            .orElseThrow(() -> new EntityNotFoundException("Enrollment not found for student " + studentId + " in period " + periodId, null));
    }

    /**
     * Get enrollment count for a period
     */
    public long getEnrollmentCountForPeriod(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        return enrollmentRepository.findByPeriodAndStatus(period, "ACTIVE").size();
    }

    /**
     * Get enrollment count for a school
     */
    public long getEnrollmentCountForSchool(String schoolCode) {
        // This could be optimized with a proper query, but for now:
        return getActiveEnrollmentsBySchoolCode(schoolCode).size();
    }

    /**
     * Check if enrollment exists by enrollment ID
     */
    public boolean existsByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findByEnrollmentId(enrollmentId).isPresent();
    }

    /**
     * Get enrollment statistics by subject for a school
     */
    public List<Object[]> getEnrollmentStatsBySubject(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
        return enrollmentRepository.countEnrollmentsBySubjectForSchool(school.getId());
    }

    /**
     * Bulk enroll students in a period
     */
    @Transactional
    public List<Enrollment> bulkEnrollStudents(List<String> studentIds, String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        
        // Check capacity
        int currentEnrollments = enrollmentRepository.findByPeriodAndStatus(period, "ACTIVE").size();
        if (period.getMaxStudents() != null && currentEnrollments + studentIds.size() > period.getMaxStudents()) {
            throw new IllegalArgumentException("Bulk enrollment would exceed period capacity");
        }
        
        return studentIds.stream()
            .map(studentId -> {
                try {
                    return enrollStudent(studentId, periodId);
                } catch (IllegalArgumentException e) {
                    // Skip already enrolled students
                    return null;
                }
            })
            .filter(enrollment -> enrollment != null)
            .toList();
    }

    // Private helper methods
    private Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId, null));
    }

    private Period getPeriodByPeriodId(String periodId) {
        return periodRepository.findByPeriodId(periodId)
            .orElseThrow(() -> new EntityNotFoundException("Period not found: " + periodId, null));
    }

    private String generateEnrollmentId() {
        return "ENR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}