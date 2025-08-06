package com.binomiaux.archimedes.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.business.ArchimedesServiceException;
import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.model.UserRegistration;
import com.binomiaux.archimedes.service.awsservices.CognitoService;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private PeriodService periodService;

    public UserRegistration registerUser(String username, String password, String email, String givenName, String familyName, String schoolCode, String userType) {
        try {
            if (cognitoService.isEmailRegistered(email)) {
                throw new ArchimedesServiceException("Email already registered: " + email, null);
            }

            String userId = null;
            if (userType.equals("teachers")) {
                Teacher teacher = new Teacher(schoolCode, null, givenName, familyName, email, username);
                teacherService.createTeacher(teacher);
                
                for (int i = 1; i <= 6; i++) {
                    periodService.createPeriod(new Period(schoolCode, teacher.getTeacherId(), String.valueOf(i), "Period " + i));
                }

                userId = teacher.getTeacherId();
            } else if (userType.equals("students")){
                Student student = new Student(schoolCode, null, givenName, familyName, email, username);
                studentService.createStudent(student);
                userId = student.getStudentId();
            }

            cognitoService.signUpUser(username, password, email, givenName, familyName);
            cognitoService.addUserToGroup(userType, username);
            cognitoService.addUserAttribute(username, "custom:userId", userId);
            UserRegistration userRegistration = new UserRegistration(userId, username, userType, false);
            return userRegistration;
        } catch (UsernameExistsException e) {
            throw new ArchimedesServiceException("Username already exists: " + username, e);
        } catch (IllegalArgumentException e) {
            cognitoService.deleteUser(username);
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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
        } catch (UserNotConfirmedException e) {
            throw new com.binomiaux.archimedes.service.exception.UserNotConfirmedException("User account is not confirmed.");
        } catch (UserNotFoundException e) {
            throw new com.binomiaux.archimedes.service.exception.UserNotFoundException("User not found.");
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
            throw new RuntimeException("Error verifying code for user: " + username,  e);
        }
    }

    public boolean verifyCode(String username, String confirmationCode) {
        try {
            cognitoService.verifyCode(username, confirmationCode);
            return true;
        } catch (Exception e) {
            // Log the exception
            System.out.println("Error verifying code: " + e.getMessage());
            return false;
        }
    }

    public void forgotPassword(String username) {
        try {
            cognitoService.forgotPassword(username);
        } catch (Exception e) {
            // Log the exception
            System.out.println("Error initiating password reset: " + e.getMessage());
            throw new RuntimeException("Error initiating password reset for user: " + username, e);
        }
    }

    public void confirmForgotPassword(String username, String newPassword, String confirmationCode) {
        try {
            cognitoService.confirmForgotPassword(username, newPassword, confirmationCode);
        } catch (Exception e) {
            throw new RuntimeException("Error initiating password reset for user: " + username, e);
        }
    }
}
