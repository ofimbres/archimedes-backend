package com.binomiaux.archimedes.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.dto.request.CreateStudentRequest;
import com.binomiaux.archimedes.dto.request.JoinPeriodRequest;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.EnrollmentRepository;
import com.binomiaux.archimedes.repository.StudentRepository;
import com.binomiaux.archimedes.service.PeriodRegistrationService.EnrollmentResult;

/**
 * Enhanced StudentService with full CRUD operations and enrollment management.
 * Implements the DynamoDB schema design patterns.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolService schoolService;
    private final IdGeneratorService idGeneratorService;
    private final PeriodRegistrationService periodRegistrationService;
    private final PeriodEnrollmentService enrollmentService; // Use existing service
    private final PeriodService periodService;

    public StudentService(StudentRepository studentRepository, 
                         EnrollmentRepository enrollmentRepository,
                         SchoolService schoolService,
                         IdGeneratorService idGeneratorService,
                         PeriodRegistrationService periodRegistrationService,
                         PeriodEnrollmentService enrollmentService,
                         PeriodService periodService) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.schoolService = schoolService;
        this.idGeneratorService = idGeneratorService;
        this.periodRegistrationService = periodRegistrationService;
        this.enrollmentService = enrollmentService;
        this.periodService = periodService;
    }

    /**
     * Creates a new student using the improved schema design.
     * Note: DynamoDB doesn't support traditional transactions like RDBMS
     */
    public Student createStudent(CreateStudentRequest request) {
        // Validate school exists
        School school = schoolService.getSchool(request.getSchoolId());
        if (school == null) {
            throw new EntityNotFoundException("School " + request.getSchoolId() + " not found", null);
        }

        // Check for duplicate email - TODO: Implement findByEmail in repository
        // if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
        //     throw new DuplicateEmailException("Student with email " + request.getEmail() + " already exists");
        // }

        // Generate sequential student ID
        String studentId = idGeneratorService.generateStudentId(request.getSchoolId());

        // Build student entity following schema design
        Student student = new Student();
        student.setPk("STUDENT#" + studentId);
        student.setSk("#METADATA");
        student.setEntityType("STUDENT");
        student.setStudentId(studentId);
        student.setSchoolId(request.getSchoolId());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setFullName(request.getFirstName() + " " + request.getLastName());
        student.setEmail(request.getEmail());
        student.setUsername(generateUsername(request.getFirstName(), request.getLastName()));
        
        // Set GSI values for efficient queries per schema (using correct method names)
        student.setParentEntityKey("SCHOOL#" + request.getSchoolId());
        student.setChildEntityKey("STUDENT#" + studentId);
        student.setSearchTypeKey("EMAIL");
        student.setSearchValueKey(request.getEmail());

        // Use repository.create() instead of save()
        studentRepository.create(student);
        return student;
    }

    /**
     * Legacy method for backward compatibility.
     */
    public void createStudent(Student student) {
        // Business logic validation - check if school exists
        School school = schoolService.getSchool(student.getSchoolId());
        if (school == null) {
            throw new EntityNotFoundException("School " + student.getSchoolId() + " not found", null);
        }
        
        // Let repository handle the creation
        studentRepository.create(student);
    }

    /**
     * Get student by ID with proper error handling.
     */
    public Student getStudentById(String schoolId, String studentId) {
        Student student = studentRepository.find(schoolId, studentId);
        if (student == null) {
            throw new EntityNotFoundException("Student " + studentId + " not found", null);
        }
        return student;
    }

    /**
     * Find student by email - useful for authentication.
     * TODO: Implement findByEmail in repository using GSI query
     */
    public Optional<Student> getStudentByEmail(String email) {
        // return studentRepository.findByEmail(email);
        return Optional.empty(); // Temporary implementation
    }

    /**
     * Get all students in a school.
     * TODO: Implement findBySchool in repository using GSI query
     */
    public List<Student> getStudentsBySchool(String schoolId) {
        // return studentRepository.findBySchool(schoolId);
        return List.of(); // Temporary implementation
    }

    /**
     * Student joins a period using a registration code.
     * Note: DynamoDB operations are atomic at item level
     */
    public EnrollmentResult joinPeriod(String schoolId, String studentId, JoinPeriodRequest request) {
        // Validate student exists
        Student student = getStudentById(schoolId, studentId);
        
        // Use the registration service to join the period
        return periodRegistrationService.useRegistrationCode(request.getRegistrationCode(), studentId);
    }

    /**
     * Get all enrollments for a student.
     */
    public List<Enrollment> getStudentEnrollments(String schoolId, String studentId) {
        return enrollmentRepository.getEnrollmentsByStudent(schoolId, studentId);
    }

    /**
     * Get all periods a student is enrolled in.
     */
    public List<Period> getStudentPeriods(String schoolId, String studentId) {
        List<Enrollment> enrollments = getStudentEnrollments(schoolId, studentId);
        return enrollments.stream()
                .map(enrollment -> periodService.getPeriod(enrollment.getPeriodId()))
                .filter(period -> period != null)
                .collect(Collectors.toList());
    }

    /**
     * Get students by period using school-scoped lookup.
     */
    public List<Student> getStudentsByPeriod(String schoolId, String periodId) {
        // Find the period within the school scope using simple period ID
        Period period = periodService.findPeriodInSchool(schoolId, periodId);
        if (period == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found in school " + schoolId, null);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.getEnrollmentsByPeriod(schoolId, periodId);
        return enrollments.stream()
                  .map(enrollment -> {
                      try {
                          return getStudentById(period.getSchoolId(), enrollment.getStudentId());
                      } catch (EntityNotFoundException e) {
                          // If student not found, create from enrollment data (fallback)
                          Student student = new Student();
                          student.setStudentId(enrollment.getStudentId());
                          student.setFirstName(enrollment.getStudentFirstName());
                          student.setLastName(enrollment.getStudentLastName());
                          student.setFullName(enrollment.getStudentFullName());
                          return student;
                      }
                  })
                  .collect(Collectors.toList());
    }

    /**
     * Update student information.
     * Note: DynamoDB operations are atomic at item level
     */
    public Student updateStudent(String schoolId, String studentId, UpdateStudentRequest request) {
        Student existingStudent = getStudentById(schoolId, studentId);

        // Check email uniqueness if email is being changed - TODO: Implement findByEmail
        if (request.getEmail() != null && !request.getEmail().equals(existingStudent.getEmail())) {
            // if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            //     throw new DuplicateEmailException("Student with email " + request.getEmail() + " already exists");
            // }
            existingStudent.setEmail(request.getEmail());
            existingStudent.setSearchValueKey(request.getEmail());
        }

        // Update other fields if provided
        if (request.getFirstName() != null) {
            existingStudent.setFirstName(request.getFirstName());
            updateFullName(existingStudent);
        }
        
        if (request.getLastName() != null) {
            existingStudent.setLastName(request.getLastName());
            updateFullName(existingStudent);
        }

        // TODO: Use repository.update() method
        // return studentRepository.save(existingStudent);
        studentRepository.update(existingStudent);
        return existingStudent;
    }

    /**
     * Delete student with proper cleanup.
     * Note: Check dependencies before deletion
     */
    public void deleteStudent(String schoolId, String studentId) {
        Student student = getStudentById(schoolId, studentId);
        
        // Check if student has active enrollments
        List<Enrollment> activeEnrollments = getStudentEnrollments(schoolId, studentId);
        if (!activeEnrollments.isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete student with active enrollments. Please unenroll from " + 
                activeEnrollments.size() + " periods first."
            );
        }

        studentRepository.delete(schoolId, studentId);
    }

    /**
     * Check if student is enrolled in a specific period.
     */
    public boolean isEnrolledInPeriod(String schoolId, String studentId, String periodId) {
        List<Enrollment> enrollments = getStudentEnrollments(schoolId, studentId);
        return enrollments.stream()
                .anyMatch(enrollment -> enrollment.getPeriodId().equals(periodId));
    }

    /**
     * Unenroll student from a period.
     * Note: Single operation call to enrollment service
     */
    public void unenrollFromPeriod(String schoolId, String studentId, String periodId) {
        // Find the period within the school scope using simple period ID
        Period period = periodService.findPeriodInSchool(schoolId, periodId);
        if (period == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found in school " + schoolId, null);
        }
        // Create simplified period ID format: T001-P001
        String simplifiedPeriodId = period.getTeacherId() + "-" + periodId;
        enrollmentService.unenrollStudent(schoolId, studentId, simplifiedPeriodId);
    }

    // Helper methods

    private String generateUsername(String firstName, String lastName) {
        return (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
    }

    private void updateFullName(Student student) {
        student.setFullName(student.getFirstName() + " " + student.getLastName());
        student.setUsername(generateUsername(student.getFirstName(), student.getLastName()));
    }

    // Placeholder for UpdateStudentRequest - to be created if needed
    public static class UpdateStudentRequest {
        private String firstName;
        private String lastName;
        private String email;

        // getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
