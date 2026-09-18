package com.tickethub.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation failures raised by request body validation.
     *
     * @param ex validation exception
     * @param request current HTTP request
     * @return a formatted bad-request response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI());
    }

    /**
     * Handles resource not found exceptions.
     *
     * @param ex the exception
     * @param request current HTTP request
     * @return a formatted not-found response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(
            final ResourceNotFoundException ex,
            final HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI());
    }

    /**
     * Handles forbidden operation exceptions.
     *
     * @param ex the exception
     * @param request current HTTP request
     * @return a formatted forbidden response
     */
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(
            final ForbiddenOperationException ex,
            final HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request.getRequestURI());
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the exception
     * @return a formatted bad-request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArg(
            final IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles illegal state exceptions.
     *
     * @param ex the exception
     * @return a formatted bad-request response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(
            final IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles generic exceptions.
     *
     * @param ex the exception
     * @param request current HTTP request
     * @return a formatted internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            final Exception ex,
            final HttpServletRequest request) {
        if (request.getRequestURI().contains("/subscribe")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request.getRequestURI());
    }

    /**
     * Handles response status exceptions.
     *
     * @param ex the exception
     * @param request current HTTP request
     * @return a formatted response based on the exception status
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(
            final ResponseStatusException ex,
            final HttpServletRequest request) {
        if (request.getRequestURI().contains("/subscribe")) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
        return buildResponse(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason(),
                request.getRequestURI());
    }

    /**
     * Handles data integrity violation exceptions.
     *
     * @param ex the exception
     * @return a formatted conflict response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(
            final DataIntegrityViolationException ex) {
        final String message =
                "Cannot delete this ticket because it has related records. "
                        + ex.getMostSpecificCause().getMessage();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", message));
    }

    /**
     * Handles access denied exceptions.
     *
     * @param ex the exception
     * @return a formatted forbidden response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            final AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Formats a field error.
     *
     * @param fieldError the field error
     * @return formatted message
     */
    private String formatFieldError(final FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    /**
     * Builds a standardized error response.
     *
     * @param status HTTP status
     * @param message error message
     * @param path request path
     * @return standardized error response
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            final HttpStatus status,
            final String message,
            final String path) {
        final ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);

        return ResponseEntity.status(status).body(body);
    }

    /**
     * Standard error response record.
     *
     * @param timestamp the timestamp
     * @param status the HTTP status code
     * @param error the HTTP status reason phrase
     * @param message the error message
     * @param path the request path
     */
    public record ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path) {
    }
}
