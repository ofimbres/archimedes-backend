# DTOs (Data Transfer Objects)

This package contains all Data Transfer Objects used for API communication.

## Structure

- `request/` - Contains request DTOs for incoming API calls
- `response/` - Contains response DTOs for outgoing API responses (future use)

## Guidelines

- Use Java records for simple immutable DTOs when possible
- Use classes with explicit getters/setters for complex DTOs that need validation or custom logic
- Keep DTOs focused on data transfer - no business logic
- Document non-obvious field purposes with JavaDoc comments

## Examples

### Simple Request DTO (Record)
```java
public record UserLoginRequest(String username, String password) {
}
```

### Complex Request DTO (Class)
```java
public class ComplexRequest {
    private String field1;
    private List<String> items;
    
    // getters and setters...
}
```
