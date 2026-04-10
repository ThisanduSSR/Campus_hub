package uk.ac.campus.domain;

/**
 * Uniform JSON envelope used by all API error responses.
 * Ensures clients always receive a structured payload rather than a raw server error.
 *
 * Example output:
 * {
 *   "httpCode": 404,
 *   "reason": "Not Found",
 *   "detail": "Room not found with ID: XYZ-999"
 * }
 */
public class ApiErrorPayload {

    private int httpCode;
    private String reason;
    private String detail;

    public ApiErrorPayload() {}

    public ApiErrorPayload(int httpCode, String reason, String detail) {
        this.httpCode = httpCode;
        this.reason = reason;
        this.detail = detail;
    }

    public int getHttpCode() { return httpCode; }
    public void setHttpCode(int httpCode) { this.httpCode = httpCode; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
