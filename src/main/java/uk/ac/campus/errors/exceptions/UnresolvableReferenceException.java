package uk.ac.campus.errors.exceptions;

/**
 * Raised when a request body contains a foreign-key reference that cannot be resolved.
 * For example: registering a sensor with a roomId that does not exist.
 * Resolves to HTTP 422 Unprocessable Entity.
 */
public class UnresolvableReferenceException extends RuntimeException {
    private final String fieldName;
    private final String referencedValue;

    public UnresolvableReferenceException(String fieldName, String referencedValue) {
        super("The field '" + fieldName + "' references '" + referencedValue
              + "', which does not exist. Ensure the target resource is created first.");
        this.fieldName = fieldName;
        this.referencedValue = referencedValue;
    }

    public String getFieldName()        { return fieldName; }
    public String getReferencedValue()  { return referencedValue; }
}
