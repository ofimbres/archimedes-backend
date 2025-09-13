package com.binomiaux.archimedes.config;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.binomiaux.archimedes.exception.business.ArchimedesServiceException;
import com.binomiaux.archimedes.exception.business.BadRequestException;
import com.binomiaux.archimedes.exception.business.ConflictException;
import com.binomiaux.archimedes.exception.business.InternalServerException;
import com.binomiaux.archimedes.exception.business.UserNotConfirmedException;
import com.binomiaux.archimedes.exception.business.UserNotFoundException;
import com.binomiaux.archimedes.model.ApiErrorResponse;

import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotConfirmedException(
            UserNotConfirmedException ex, WebRequest request) {
        log.warning("User not confirmed: " + ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "USR_001", "User account is not confirmed", ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        log.warning("User not found: " + ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "USR_002", "User not found", ex.getMessage(), request);
    }

    @ExceptionHandler(ArchimedesServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleArchimedesServiceException(
            ArchimedesServiceException ex, WebRequest request) {
        log.warning("Business logic error: " + ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "BUS_001", "Business logic error", ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        log.warning("Bad request: " + ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "REQ_001", "Bad request", ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(
            ConflictException ex, WebRequest request) {
        log.warning("Conflict: " + ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "CON_001", "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ApiErrorResponse> handleInternalServerException(
            InternalServerException ex, WebRequest request) {
        log.severe("Internal server error: " + ex.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SRV_001", "Internal server error", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warning("Access denied: " + ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "SEC_001", "Access denied", 
                "You don't have permission to access this resource", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warning("Validation failed: " + ex.getMessage());
        
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ApiErrorResponse error = new ApiErrorResponse("VAL_001", "Validation failed", validationErrors);
        error.setPath(getPath(request));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warning("Type mismatch: " + ex.getMessage());
        
        String details = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "VAL_002", "Invalid parameter type", details, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warning("Illegal argument: " + ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "VAL_003", "Invalid argument", ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameExistsException(
            UsernameExistsException ex, WebRequest request) {
        log.warning("Username already exists: " + ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "USR_003", "Username already exists", 
                "The provided username is already taken", request);
    }

    @ExceptionHandler(DynamoDbException.class)
    public ResponseEntity<ApiErrorResponse> handleDynamoDbException(
            DynamoDbException ex, WebRequest request) {
        log.severe("DynamoDB error: " + ex.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "DB_001", "Database error", 
                "A database error occurred. Please try again later.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.severe("Unexpected error: " + ex.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Internal server error", 
                "An unexpected error occurred. Please try again later.", request);
    }

    // Utility methods to reduce duplication
    private ResponseEntity<ApiErrorResponse> createErrorResponse(
            HttpStatus status, String errorCode, String message, String details, WebRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(errorCode, message, details);
        error.setPath(getPath(request));
        return ResponseEntity.status(status).body(error);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}