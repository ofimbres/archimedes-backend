# Exception Handling

This package contains all custom exceptions used throughout the application, organized by purpose and scope.

## Structure

### `common/`
Contains general-purpose exceptions that can be used across multiple layers:
- `EntityNotFoundException` - When a requested entity cannot be found
- `ConflictOperationException` - When an operation conflicts with existing data/state

### `business/`
Contains business logic specific exceptions:
- `UserNotFoundException` - When a user cannot be found during authentication/operations
- `UserNotConfirmedException` - When a user account exists but is not confirmed
- `ArchimedesServiceException` - General service layer exception for business operations

## Guidelines

- **Use specific exceptions** rather than generic ones when possible
- **Include meaningful error messages** that help with debugging
- **Follow the principle**: Common exceptions for technical/data issues, business exceptions for domain-specific problems
- **Add new exceptions** to the appropriate subdirectory based on their scope

## Exception Handling Flow

1. **Services** throw business-specific exceptions
2. **Repositories** throw common exceptions for data-related issues  
3. **GlobalExceptionHandler** (`app/config/`) catches and converts all exceptions to appropriate HTTP responses

## Example Usage

```java
// In a service
if (user == null) {
    throw new UserNotFoundException("User with ID " + userId + " not found");
}

// In a repository
if (entity == null) {
    throw new EntityNotFoundException("Entity not found", null);
}
```
