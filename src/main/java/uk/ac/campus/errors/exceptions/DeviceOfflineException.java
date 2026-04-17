package uk.ac.campus.errors.exceptions;

/**
 * Raised when a new telemetry reading is submitted for a sensor that is
 * currently in MAINTENANCE state and therefore cannot accept data.
 * Resolves to HTTP 403 Forbidden.
 */
public class DeviceOfflineException extends RuntimeException {
    private final String sensorId;

    public DeviceOfflineException(String sensorId) {
        super("Sensor '" + sensorId + "' is under active maintenance and is not accepting "
              + "new telemetry. Change its status to ACTIVE before submitting readings.");
        this.sensorId = sensorId;
    }

    public String getSensorId() { return sensorId; }
}
