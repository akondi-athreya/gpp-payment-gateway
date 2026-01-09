package com.example.gateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.gateway.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "Validation error";
        
        // Extract the actual message or use default
        String description = errorMessage;
        if (errorMessage.contains("minimum value")) {
            description = "amount must be at least 100";
        } else if (errorMessage.contains("Amount is required")) {
            description = "amount is required";
        }
        
        ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException for payment validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        
        if (message == null) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", "Invalid request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Parse error code and description from message format: "CODE|Description"
        if (message.contains("|")) {
            String[] parts = message.split("\\|", 2);
            String code = parts[0];
            String description = parts[1];
            ErrorResponse errorResponse = new ErrorResponse(code, description);

            // Return appropriate HTTP status based on error code
            if (code.equals("AUTHENTICATION_ERROR")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            } else if (code.equals("NOT_FOUND_ERROR")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }

        ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
