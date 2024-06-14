package com.binomiaux.archimedes.service.awsservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;
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

    public AdminCreateUserResponse createUser(String username, String password, String email, String givenName,
            String familyName) {
        List<AttributeType> userAttributes = new ArrayList<>();
        userAttributes.add(AttributeType.builder().name("email").value(email).build());
        userAttributes.add(AttributeType.builder().name("given_name").value(givenName).build());
        userAttributes.add(AttributeType.builder().name("family_name").value(familyName).build());

        AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .userAttributes(userAttributes)
                .temporaryPassword(password)
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .build();

        return cognitoIdentityProviderClient.adminCreateUser(createUserRequest);
    }

    public AdminAddUserToGroupResponse addUserToGroup(String userType, String username) {
        return cognitoIdentityProviderClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                .groupName(userType)
                .userPoolId(userPoolId)
                .username(username)
                .build());
    }

    public AdminInitiateAuthResponse loginUser(String username, String password) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .authParameters(authParameters)
                .build();

        return cognitoIdentityProviderClient.adminInitiateAuth(authRequest);
    }
}