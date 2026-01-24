package devybigboard.exceptions;

/**
 * Exception thrown when a draft is not found by UUID.
 */
public class DraftNotFoundException extends RuntimeException {
    
    public DraftNotFoundException(String uuid) {
        super("Draft not found with UUID: " + uuid);
    }
    
    public DraftNotFoundException(String uuid, Throwable cause) {
        super("Draft not found with UUID: " + uuid, cause);
    }
}
