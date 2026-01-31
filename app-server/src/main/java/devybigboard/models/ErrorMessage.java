package devybigboard.models;

import java.time.LocalDateTime;

/**
 * WebSocket message for communicating errors to clients.
 */
public class ErrorMessage {
    
    private String message;
    private String code;
    private LocalDateTime timestamp;
    
    // Constructors
    public ErrorMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorMessage(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorMessage(String message, String code, LocalDateTime timestamp) {
        this.message = message;
        this.code = code;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
