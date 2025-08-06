# Configuration

This package contains all Spring Boot configuration classes, organized by domain and functionality.

## Structure

### Main Configuration (`/config/`)
- `GlobalExceptionHandler.java` - Global exception handling for REST APIs
- `JacksonConfig.java` - JSON serialization/deserialization configuration
- `OpenApiConfig.java` - API documentation (Swagger/OpenAPI) configuration

### AWS Configuration (`/config/aws/`)
Contains all AWS service client configurations:
- `DynamoDbConfig.java` - DynamoDB client setup
- `DynamoDbProperties.java` - DynamoDB configuration properties  
- `CognitoConfig.java` - Cognito Identity Provider client setup
- `S3Config.java` - S3 client and presigner setup

### Web Configuration (`/config/web/`)
Contains web-layer specific configurations:
- `CognitoSecurityConfiguration.java` - Security configuration with Cognito authentication
- `Cors.java` - CORS configuration properties

## Guidelines

- **Single Responsibility**: Each configuration class should focus on one specific area
- **Environment Specific**: Use `@ConfigurationProperties` for externalized configuration
- **AWS Best Practices**: Use default credential providers and consistent region configuration
- **Security**: Keep security configurations in the web package for clear separation

## Configuration Loading

All configurations are automatically discovered by Spring Boot's component scanning since they're in the main package structure. The `@Configuration` and `@Component` annotations ensure proper bean registration.

## Example Usage

### Properties Configuration
```java
@ConfigurationProperties(prefix = "dynamodb")
public class DynamoDbProperties {
    private String tableName;
    // getters and setters...
}
```

### Bean Configuration
```java
@Configuration
public class AwsConfig {
    @Bean
    public SomeAwsClient someAwsClient() {
        return SomeAwsClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.US_WEST_2)
            .build();
    }
}
```
