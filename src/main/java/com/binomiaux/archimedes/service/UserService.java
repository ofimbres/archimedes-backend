package com.binomiaux.archimedes.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.business.ArchimedesServiceException;
import com.binomiaux.archimedes.exception.business.UserNotConfirmedException;
import com.binomiaux.archimedes.exception.business.UserNotFoundException;
import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.model.UserRegistration;
import com.binomiaux.archimedes.service.awsservices.CognitoService;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String TEACHER_USER_TYPE = "teachers";
    private static final String STUDENT_USER_TYPE = "students";
    private static final int DEFAULT_PERIODS_PER_TEACHER = 6;

    private final CognitoService cognitoService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final PeriodService periodService;

    public UserService(CognitoService cognitoService, 
                      TeacherService teacherService,
                      StudentService studentService, 
                      PeriodService periodService) {
        this.cognitoService = cognitoService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.periodService = periodService;
    }

    public UserRegistration registerUser(String username, String password, String email, 
                                        String givenName, String familyName, String schoolCode, String userType) {
        try {
            // Validate email is not already registered
            if (cognitoService.isEmailRegistered(email)) {
                throw new ArchimedesServiceException("Email already registered: " + email, null);
            }

            // Create the appropriate user type and get their ID
            String userId = createUserByType(userType, schoolCode, givenName, familyName, email, username);

            // Register user in Cognito
            cognitoService.signUpUser(username, password, email, givenName, familyName);
            cognitoService.addUserToGroup(userType, username);
            cognitoService.addUserAttribute(username, "custom:userId", userId);
            
            return new UserRegistration(userId, username, userType, false);
            
        } catch (UsernameExistsException e) {
            throw new ArchimedesServiceException("Username already exists: " + username, e);
        } catch (IllegalArgumentException e) {
            // Cleanup on failure
            cognitoService.deleteUser(username);
            throw new ArchimedesServiceException("Invalid user data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Cleanup on failure
            cognitoService.deleteUser(username);
            throw new ArchimedesServiceException("Failed to register user: " + e.getMessage(), e);
        }
    }

    private String createUserByType(String userType, String schoolCode, String givenName, 
                                   String familyName, String email, String username) {
        switch (userType) {
            case TEACHER_USER_TYPE:
                return createTeacherUser(schoolCode, givenName, familyName, email, username);
            case STUDENT_USER_TYPE:
                return createStudentUser(schoolCode, givenName, familyName, email, username);
            default:
                throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    private String createTeacherUser(String schoolCode, String givenName, String familyName, 
                                   String email, String username) {
        Teacher teacher = new Teacher(generateTeacherId(schoolCode), schoolCode, givenName, 
                                    familyName, email, username);
        teacherService.createTeacher(teacher);
        
        // Create default periods for the teacher
        createDefaultPeriodsForTeacher(teacher);
        
        return teacher.getTeacherId();
    }

    private String createStudentUser(String schoolCode, String givenName, String familyName, 
                                   String email, String username) {
        Student student = new Student(generateStudentId(schoolCode), schoolCode, givenName, 
                                    familyName, email, username);
        studentService.createStudent(student);
        return student.getStudentId();
    }

    private void createDefaultPeriodsForTeacher(Teacher teacher) {
        for (int i = 1; i <= DEFAULT_PERIODS_PER_TEACHER; i++) {
            String periodId = "PER_" + teacher.getTeacherId() + "_" + i;
            String periodName = "Period " + i;
            Period period = new Period(periodId, teacher.getSchoolId(), teacher.getTeacherId(), i, periodName,
                                     teacher.getFirstName(), teacher.getLastName());
            periodService.createPeriod(period);
        }
    }

    public LoggedInUser loginUser(String username, String password) {
        try {
            log.info("Logging in user: {}", username);
            InitiateAuthResponse authResponse = cognitoService.loginUser(username, password);
            String accessToken = authResponse.authenticationResult().accessToken();
            //String idToken = authResponse.authenticationResult().idToken();
            //String refreshToken = authResponse.authenticationResult().refreshToken();

            LoggedInUser loggedInUser = new LoggedInUser(username, accessToken);
            return loggedInUser;
        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException e) {
            throw new UserNotConfirmedException("User account is not confirmed.");
        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException e) {
            throw new UserNotFoundException("User not found.");
        }
    }

    public Map<String, String> getUserAttributes(String username) {
        return cognitoService.getUserAttributes(username).userAttributes().stream()
            .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    }

    public void sendCode(String username) {
        try {
            cognitoService.sendCode(username);
        } catch (Exception e) {
            log.error("Error sending verification code for user: {}", username, e);
            throw new ArchimedesServiceException("Failed to send verification code", e);
        }
    }

    public boolean verifyCode(String username, String confirmationCode) {
        try {
            cognitoService.verifyCode(username, confirmationCode);
            return true;
        } catch (Exception e) {
            log.error("Error verifying code for user: {}", username, e);
            return false;
        }
    }

    public void forgotPassword(String username) {
        try {
            cognitoService.forgotPassword(username);
        } catch (Exception e) {
            log.error("Error initiating password reset for user: {}", username, e);
            throw new ArchimedesServiceException("Failed to initiate password reset", e);
        }
    }

    public void confirmForgotPassword(String username, String newPassword, String confirmationCode) {
        try {
            cognitoService.confirmForgotPassword(username, newPassword, confirmationCode);
        } catch (Exception e) {
            log.error("Error confirming password reset for user: {}", username, e);
            throw new ArchimedesServiceException("Failed to confirm password reset", e);
        }
    }

    // Helper methods for generating unique IDs
    private String generateTeacherId(String schoolCode) {
        return "TCH_" + schoolCode + "_" + System.currentTimeMillis();
    }

    private String generateStudentId(String schoolCode) {
        return "STU_" + schoolCode + "_" + System.currentTimeMillis();
    }
}
