package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.model.UserRegistration;
import com.binomiaux.archimedes.service.PeriodService;
import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.service.TeacherService;
import com.binomiaux.archimedes.service.UserService;
import com.binomiaux.archimedes.service.awsservices.CognitoService;

import com.binomiaux.archimedes.service.exception.ArchimedesServiceException;
import com.binomiaux.archimedes.service.exception.EmailAlreadyInUseException;
import com.binomiaux.archimedes.service.exception.UserAlreadyExistsException;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private PeriodService periodService;

    @Override
    public UserRegistration registerUser(String username, String password, String email, String givenName, String familyName, String schoolCode, String userType) {
        try {
            if (cognitoService.isEmailRegistered(email)) {
                throw new EmailAlreadyInUseException("Email already registered: " + email, null);
            }

            cognitoService.signUpUser(username, password, email, givenName, familyName);
            cognitoService.addUserToGroup(userType, username);

            String userId = null;
            if (userType.equals("teachers")) {
                Teacher teacher = new Teacher(schoolCode, null, givenName, familyName, email, username);
                teacherService.create(teacher);
                
                for (int i = 1; i <= 6; i++) {
                    periodService.create(new Period(schoolCode, teacher.getTeacherId(), String.valueOf(i), "Period " + i));
                }

                userId = teacher.getTeacherId();
            } else if (userType.equals("students")){
                Student student = new Student(schoolCode, null, givenName, familyName, email, username);
                studentService.create(student);
                userId = student.getStudentId();
            }

            UserRegistration userRegistration = new UserRegistration(userId, username, userType, false);
            return userRegistration;
        } catch (UsernameExistsException e) {
            throw new UserAlreadyExistsException("Username already exists: " + username, e);
        } catch (IllegalArgumentException e) {
            cognitoService.deleteUser(username);
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public LoggedInUser loginUser(String username, String password) {
        try {
            InitiateAuthResponse authResponse = cognitoService.loginUser(username, password);
            String accessToken = authResponse.authenticationResult().accessToken();
            //String idToken = authResponse.authenticationResult().idToken();
            //String refreshToken = authResponse.authenticationResult().refreshToken();

            LoggedInUser loggedInUser = new LoggedInUser(username, accessToken);
            return loggedInUser;
        } catch (UserNotConfirmedException e) {
            throw new ArchimedesServiceException("User not confirmed: " + username + ". Please confirm your account with the code sent to your email.", e);
        } catch (Exception e) {
            // Handle login errors
            return null;
        }
    }

    @Override
    public Map<String, String> getUserAttributes(String username) {
        return cognitoService.getUserAttributes(username).userAttributes().stream()
            .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    }

    @Override
    public void sendCode(String username) {
        try {
            cognitoService.sendCode(username);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying code for user: " + username,  e);
        }
    }

    @Override
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

    @Override
    public void forgotPassword(String username) {
        try {
            cognitoService.forgotPassword(username);
        } catch (Exception e) {
            // Log the exception
            System.out.println("Error initiating password reset: " + e.getMessage());
            throw new RuntimeException("Error initiating password reset for user: " + username, e);
        }
    }

    @Override
    public void confirmForgotPassword(String username, String newPassword, String confirmationCode) {
        try {
            cognitoService.confirmForgotPassword(username, newPassword, confirmationCode);
        } catch (Exception e) {
            throw new RuntimeException("Error initiating password reset for user: " + username, e);
        }
    }
}
