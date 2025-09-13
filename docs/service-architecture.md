# Service Architecture Documentation

## Overview

This document outlines the clear separation of concerns between our service layers to maintain clean architecture principles.

## Service Responsibilities

### 1. UserService (Authentication Layer)
**Purpose**: Handles authentication and user account management with AWS Cognito
- `loginUser()` - Authenticate user credentials
- `isEmailRegistered()` - Check if email exists in Cognito
- `createAuthenticationUser()` - Create Cognito user account
- `sendVerificationCode()` - Email verification flows
- `verifyCode()` - Verification code validation
- `forgotPassword()` - Password reset initiation
- `confirmForgotPassword()` - Password reset completion
- `getUserAttributes()` - Retrieve user profile data

**Key Characteristics**:
- No business domain knowledge
- Focused purely on authentication concerns
- Interacts only with AWS Cognito
- Returns authentication-specific objects

### 2. RegistrationService (Orchestration Layer)
**Purpose**: Coordinates complete user registration workflow
- `registerUser()` - Orchestrates full registration process

**Workflow**:
1. Validate registration request
2. Create authentication user via `UserService.createAuthenticationUser()`
3. Create domain entity (Student/Teacher) via appropriate domain service
4. Return unified registration response

**Key Characteristics**:
- Bridges authentication and domain layers
- No direct business logic
- Handles transaction coordination
- Single point for registration orchestration

### 3. Domain Services (Business Logic Layer)

#### StudentService
**Purpose**: Student entity business logic and lifecycle management
- `createStudent()` - Create new student with school association
- `getStudentById()` - Retrieve student by composite key
- `updateStudent()` - Update student information
- `deleteStudent()` - Remove student from system
- `getAllStudentsBySchool()` - School-scoped student listing

#### TeacherService  
**Purpose**: Teacher entity business logic and lifecycle management
- `createTeacher()` - Create new teacher with default periods
- `getTeacherById()` - Retrieve teacher by composite key
- `updateTeacher()` - Update teacher information
- `deleteTeacher()` - Remove teacher from system
- `getAllTeachersBySchool()` - School-scoped teacher listing

**Key Characteristics**:
- School-scoped operations (all methods require `schoolId`)
- Domain-specific business rules
- Entity lifecycle management
- No authentication concerns

### 4. Other Domain Services

#### PeriodService
- Period entity management
- Teacher-period associations

#### SchoolService
- School entity management
- School validation logic

## Controller Integration

### UserController
- Uses `UserService` for authentication endpoints (login, password reset, verification)
- Uses `RegistrationService` for user registration
- No direct calls to domain services

### Domain Controllers (StudentController, TeacherController, etc.)
- Use respective domain services directly
- Handle school-scoped operations
- Return clean DTOs via response mappers

## Benefits of This Architecture

1. **Clear Separation of Concerns**: Each service has a single, well-defined responsibility
2. **Maintainability**: Changes to authentication don't affect business logic and vice versa
3. **Testability**: Each layer can be tested independently
4. **Scalability**: Services can evolve independently
5. **Code Reusability**: Domain services can be reused across different controllers

## Migration Notes

- `UserService` was refactored to remove domain logic (student/teacher creation)
- Registration orchestration moved to dedicated `RegistrationService`
- All domain services now require `schoolId` for proper scoping
- Controllers updated to use appropriate service layer

This architecture follows Spring Boot best practices and Domain-Driven Design principles while maintaining simplicity and avoiding over-engineering.
