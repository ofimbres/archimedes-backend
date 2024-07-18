package com.binomiaux.archimedes.service.awsservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResendConfirmationCodeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResendConfirmationCodeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CognitoService {

    @Value("${cognito.user-pool-id}")
    private String userPoolId;

    @Value("${cognito.user-pool-client-id}")
    private String clientId;

    @Autowired
    private CognitoIdentityProviderClient cognitoIdentityProviderClient;

    public SignUpResponse signUpUser(String username, String password, String email, String givenName, String familyName) {
        List<AttributeType> userAttributes = new ArrayList<>();
        userAttributes.add(AttributeType.builder().name("email").value(email).build());
        userAttributes.add(AttributeType.builder().name("given_name").value(givenName).build());
        userAttributes.add(AttributeType.builder().name("family_name").value(familyName).build());

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .username(username)
                .password(password)
                .userAttributes(userAttributes)
                .build();

        return cognitoIdentityProviderClient.signUp(signUpRequest);
    }

    public AdminAddUserToGroupResponse addUserToGroup(String userType, String username) {
        return cognitoIdentityProviderClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                .groupName(userType)
                .userPoolId(userPoolId)
                .username(username)
                .build());
    }

    public InitiateAuthResponse loginUser(String username, String password) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParameters)
                .build();

        return cognitoIdentityProviderClient.initiateAuth(authRequest);
    }

    public AdminGetUserResponse getUserAttributes(String username) {
        AdminGetUserRequest userRequest = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();
    
        return cognitoIdentityProviderClient.adminGetUser(userRequest);
    }

    public ResendConfirmationCodeResponse sendCode(String username) {
        ResendConfirmationCodeRequest resendConfirmationCodeRequest = ResendConfirmationCodeRequest.builder()
            .clientId(clientId)
            .username(username)
            .build();

        return cognitoIdentityProviderClient.resendConfirmationCode(resendConfirmationCodeRequest);
    }

    public ConfirmSignUpResponse verifyCode(String username, String confirmationCode) {
        ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
            .clientId(clientId)
            .username(username)
            .confirmationCode(confirmationCode)
            .build();

        return cognitoIdentityProviderClient.confirmSignUp(confirmSignUpRequest);
    }

    public ForgotPasswordResponse forgotPassword(String username) {
        ForgotPasswordRequest forgotPasswordRequest = ForgotPasswordRequest.builder()
            .clientId(clientId)
            .username(username)
            .build();

        return cognitoIdentityProviderClient.forgotPassword(forgotPasswordRequest);
    }

    public ConfirmForgotPasswordResponse confirmForgotPassword(String username, String password, String confirmationCode) {
        ConfirmForgotPasswordRequest confirmForgotPasswordRequest = ConfirmForgotPasswordRequest.builder()
            .clientId(clientId)
            .username(username)
            .password(password)
            .confirmationCode(confirmationCode)
            .build();

        return cognitoIdentityProviderClient.confirmForgotPassword(confirmForgotPasswordRequest);
    }

    public ChangePasswordResponse changePassword(String accessToken, String previousPassword, String proposedPassword) {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
            .accessToken(accessToken)
            .previousPassword(previousPassword)
            .proposedPassword(proposedPassword)
            .build();

        return cognitoIdentityProviderClient.changePassword(changePasswordRequest);
    }

    public AdminDeleteUserResponse deleteUser(String username) {
        AdminDeleteUserRequest adminDeleteUserRequest = AdminDeleteUserRequest.builder()
            .userPoolId(userPoolId)
            .username(username)
            .build();        
        return cognitoIdentityProviderClient.adminDeleteUser(adminDeleteUserRequest);
    }
}