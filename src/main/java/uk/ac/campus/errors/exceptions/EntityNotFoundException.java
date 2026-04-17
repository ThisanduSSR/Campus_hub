package uk.ac.campus.errors.exceptions;

/**
 * Raised when a URL path segment references a resource that cannot be located.
 * Resolves to HTTP 404 Not Found.
 */
public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final String entityId;

    public EntityNotFoundException(String entityType, String entityId) {
        super(entityType + " with identifier '" + entityId + "' could not be found.");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() { return entityType; }
    public String getEntityId()   { return entityId; }
}
