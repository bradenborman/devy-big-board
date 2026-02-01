package devybigboard.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API endpoints.
 * Handles all exceptions and returns standardized ErrorResponse objects.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Check if the request is from a browser (wants HTML response)
     */
    private boolean isBrowserRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("text/html");
    }
    
    /**
     * Handle PlayerNotFoundException - returns 404
     */
    @ExceptionHandler(PlayerNotFoundException.class)
    public Object handlePlayerNotFoundException(
            PlayerNotFoundException ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Player not found: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle DraftNotFoundException - returns 404
     */
    @ExceptionHandler(DraftNotFoundException.class)
    public Object handleDraftNotFoundException(
            DraftNotFoundException ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Draft not found: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle UnauthorizedException - returns 403
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Object handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Unauthorized access: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle ValidationException - returns 400
     */
    @ExceptionHandler(ValidationException.class)
    public Object handleValidationException(
            ValidationException ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle MethodArgumentNotValidException - returns 400 with field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Validation failed: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed for one or more fields",
            request.getDescription(false).replace("uri=", ""),
            fieldErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle DataAccessException - returns 503
     */
    @ExceptionHandler(DataAccessException.class)
    public Object handleDataAccessException(
            DataAccessException ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Database error: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Unavailable",
            "Database service is temporarily unavailable. Please try again later.",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions - returns 500
     */
    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(
            Exception ex, WebRequest request, HttpServletRequest httpRequest) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        if (isBrowserRequest(httpRequest)) {
            return new ModelAndView("forward:/index.html");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
