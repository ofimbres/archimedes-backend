package com.binomiaux.archimedes.service.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.Student;
import com.binomiaux.archimedes.entity.Teacher;
import com.binomiaux.archimedes.exception.business.ConflictException;
import com.binomiaux.archimedes.model.UserRegistration;
import com.binomiaux.archimedes.repository.jpa.SchoolRepository;
import com.binomiaux.archimedes.service.UserService;

/**
 * JPA version of RegistrationService: Orchestrates the complete user registration process.
 * Uses only JPA entities and services.
 * 
 * Responsibilities:
 * - Coordinate user registration workflow
 * - Bridge authentication layer with JPA domain layer
 * - Handle registration business logic using JPA services
 * 
 * Does NOT handle:
 * - Direct authentication operations (delegates to UserService)
 * - Domain entity business logic (delegates to JPA services)
 */
@Service
@Transactional
public class RegistrationJpaService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationJpaService.class);
    private static final String TEACHER_USER_TYPE = "teachers";
    private static final String STUDENT_USER_TYPE = "students";

    private final UserService userService;
    private final TeacherJpaService teacherJpaService;
    private final StudentJpaService studentJpaService;
    private final SchoolRepository schoolRepository;
    private final SequentialIdGeneratorService idGeneratorService;

    public RegistrationJpaService(UserService userService,
                                 TeacherJpaService teacherJpaService,
                                 StudentJpaService studentJpaService,
                                 SchoolRepository schoolRepository,
                                 SequentialIdGeneratorService idGeneratorService) {
        this.userService = userService;
        this.teacherJpaService = teacherJpaService;
        this.studentJpaService = studentJpaService;
        this.schoolRepository = schoolRepository;
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Complete user registration: creates both authentication user and domain entity.
     */
    @Transactional
    public UserRegistration registerUser(String username, String password, String email, 
                                        String givenName, String familyName, Long schoolId, String userType) {
        
        // Step 1: Validate email availability (authentication layer)
        if (userService.isEmailRegistered(email)) {
            throw new ConflictException("Email already registered: " + email);
        }

        // Step 2: Create domain entity first (get the domain ID)
        String domainEntityId = createDomainEntity(userType, schoolId, givenName, familyName, email, username);

        // Step 3: Create authentication user (with reference to domain entity)
        userService.createAuthenticationUser(username, password, email, givenName, familyName, userType, domainEntityId);
        
        log.info("Successfully registered {} user: {} with domain ID: {}", userType, username, domainEntityId);
        return new UserRegistration(domainEntityId, username, userType, false);
    }

    /**
     * Register a teacher with minimal information
     */
    @Transactional
    public UserRegistration registerTeacher(String username, String password, String email,
                                           String firstName, String lastName, Long schoolId) {
        return registerUser(username, password, email, firstName, lastName, schoolId, TEACHER_USER_TYPE);
    }

    /**
     * Register a student with minimal information
     */
    @Transactional
    public UserRegistration registerStudent(String username, String password, String email,
                                           String firstName, String lastName, Long schoolId) {
        return registerUser(username, password, email, firstName, lastName, schoolId, STUDENT_USER_TYPE);
    }

    /**
     * Register a teacher with additional details
     */
    @Transactional
    public UserRegistration registerTeacherWithDetails(String username, String password, String email,
                                                      String firstName, String lastName, Long schoolId, 
                                                      String department) {
        // Step 1: Validate email availability
        if (userService.isEmailRegistered(email)) {
            throw new ConflictException("Email already registered: " + email);
        }

        // Step 2: Create teacher with department
        Teacher teacher = teacherJpaService.createTeacher(schoolId, firstName, lastName, email, department);

        // Step 3: Create authentication user
        userService.createAuthenticationUser(username, password, email, firstName, lastName, TEACHER_USER_TYPE, teacher.getTeacherId());
        
        log.info("Successfully registered teacher with department: {} for user: {}", department, username);
        return new UserRegistration(teacher.getTeacherId(), username, TEACHER_USER_TYPE, false);
    }

    /**
     * Creates the appropriate domain entity based on user type.
     */
    private String createDomainEntity(String userType, Long schoolId, String givenName, 
                                     String familyName, String email, String username) {
        switch (userType) {
            case TEACHER_USER_TYPE:
                return createTeacherEntity(schoolId, givenName, familyName, email, username);
            case STUDENT_USER_TYPE:
                return createStudentEntity(schoolId, givenName, familyName, email, username);
            default:
                throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    private String createTeacherEntity(Long schoolId, String givenName, String familyName, 
                                      String email, String username) {
        log.info("Creating teacher entity for school: {} with email: {}", schoolId, email);
        
        try {
            // Create teacher using JPA service
            Teacher teacher = teacherJpaService.createTeacher(schoolId, givenName, familyName, email, null);
            
            log.info("Created teacher with ID: {} for school: {}", teacher.getTeacherId(), schoolId);
            return teacher.getTeacherId();
            
        } catch (Exception e) {
            log.error("Failed to create teacher entity for school: {} with email: {}", schoolId, email, e);
            throw new RuntimeException("Failed to create teacher entity: " + e.getMessage(), e);
        }
    }

    private String createStudentEntity(Long schoolId, String givenName, String familyName, 
                                      String email, String username) {
        log.info("Creating student entity for school: {} with email: {}", schoolId, email);
        
        try {
            schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            
            String studentId = generateStudentId(schoolId);
            Student student = studentJpaService.createStudent(studentId, schoolId, givenName, familyName, email);
            
            log.info("Created student with ID: {} for school: {}", student.getStudentId(), schoolId);
            return student.getStudentId();
            
        } catch (Exception e) {
            log.error("Failed to create student entity for school: {} with email: {}", schoolId, email, e);
            throw new RuntimeException("Failed to create student entity: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a user can be registered (email not already taken)
     */
    public boolean canRegisterUser(String email) {
        return !userService.isEmailRegistered(email);
    }

    /**
     * Generate a student ID for registration using sequential IDs per school
     */
    private String generateStudentId(Long schoolId) {
        return idGeneratorService.generateStudentId(schoolId);
    }

    /**
     * Get registration statistics
     */
    public RegistrationStats getRegistrationStats(Long schoolId) {
        try {
            long teacherCount = teacherJpaService.getTeacherCountBySchoolId(schoolId);
            // For student count, we'll get all students from school and count them
            long studentCount = studentJpaService.getStudentsBySchool(schoolId).size();
            
            return new RegistrationStats(schoolId, teacherCount, studentCount);
            
        } catch (Exception e) {
            log.warn("Could not get registration stats for school: {}", schoolId, e);
            return new RegistrationStats(schoolId, 0, 0);
        }
    }

    /**
     * Registration statistics holder
     */
    public static class RegistrationStats {
        private final Long schoolId;
        private final long teacherCount;
        private final long studentCount;

        public RegistrationStats(Long schoolId, long teacherCount, long studentCount) {
            this.schoolId = schoolId;
            this.teacherCount = teacherCount;
            this.studentCount = studentCount;
        }

        public Long getSchoolId() { return schoolId; }
        public long getTeacherCount() { return teacherCount; }
        public long getStudentCount() { return studentCount; }
        public long getTotalCount() { return teacherCount + studentCount; }
    }
}