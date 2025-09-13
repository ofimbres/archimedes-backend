package com.binomiaux.archimedes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.business.ConflictException;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.model.UserRegistration;

/**
 * RegistrationService: Orchestrates the complete user registration process.
 * 
 * Responsibilities:
 * - Coordinate user registration workflow
 * - Bridge authentication layer with domain layer
 * - Handle registration business logic
 * 
 * Does NOT handle:
 * - Direct authentication operations (delegates to UserService)
 * - Domain entity business logic (delegates to TeacherService/StudentService)
 */
@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    private static final String TEACHER_USER_TYPE = "teachers";
    private static final String STUDENT_USER_TYPE = "students";

    private final UserService userService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final IdGeneratorService idGeneratorService;

    public RegistrationService(UserService userService,
                              TeacherService teacherService,
                              StudentService studentService,
                              IdGeneratorService idGeneratorService) {
        this.userService = userService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Complete user registration: creates both authentication user and domain entity.
     */
    public UserRegistration registerUser(String username, String password, String email, 
                                        String givenName, String familyName, String schoolCode, String userType) {
        
        // Step 1: Validate email availability (authentication layer)
        if (userService.isEmailRegistered(email)) {
            throw new ConflictException("Email already registered: " + email);
        }

        // Step 2: Create domain entity first (get the domain ID)
        String domainEntityId = createDomainEntity(userType, schoolCode, givenName, familyName, email, username);

        // Step 3: Create authentication user (with reference to domain entity)
        userService.createAuthenticationUser(username, password, email, givenName, familyName, userType, domainEntityId);
        
        log.info("Successfully registered {} user: {} with domain ID: {}", userType, username, domainEntityId);
        return new UserRegistration(domainEntityId, username, userType, false);
    }

    /**
     * Creates the appropriate domain entity based on user type.
     */
    private String createDomainEntity(String userType, String schoolCode, String givenName, 
                                     String familyName, String email, String username) {
        switch (userType) {
            case TEACHER_USER_TYPE:
                return createTeacherEntity(schoolCode, givenName, familyName, email, username);
            case STUDENT_USER_TYPE:
                return createStudentEntity(schoolCode, givenName, familyName, email, username);
            default:
                throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    private String createTeacherEntity(String schoolCode, String givenName, String familyName, 
                                      String email, String username) {
        // Convert school code to school ID (assuming they're the same for now, or you might need a lookup)
        String schoolId = schoolCode; // TODO: Convert schoolCode to schoolId if different
        
        // Generate auto-incremented teacher ID
        String teacherId = idGeneratorService.generateTeacherId(schoolId);
        
        Teacher teacher = new Teacher(teacherId, schoolId, givenName, 
                                    familyName, email, username);
        teacherService.createTeacher(teacher);
        return teacher.getTeacherId();
    }

    private String createStudentEntity(String schoolCode, String givenName, String familyName, 
                                      String email, String username) {
        // Convert school code to school ID (assuming they're the same for now, or you might need a lookup)
        String schoolId = schoolCode; // TODO: Convert schoolCode to schoolId if different
        
        log.info("Creating student entity for school: {} (schoolCode: {})", schoolId, schoolCode);
        
        // Generate auto-incremented student ID  
        String studentId = idGeneratorService.generateStudentId(schoolId);
        
        log.info("Generated student ID: {} for school: {}", studentId, schoolId);
        
        Student student = new Student(studentId, schoolId, givenName, 
                                    familyName, email, username);
        studentService.createStudent(student);
        return student.getStudentId();
    }
}
