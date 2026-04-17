package uk.ac.campus.errors.exceptions;

/**
 * Raised when a room deletion is attempted but the room still hosts active sensors.
 * Resolves to HTTP 409 Conflict.
 */
public class RoomOccupiedException extends RuntimeException {
    private final int deviceCount;

    public RoomOccupiedException(String roomId, int deviceCount) {
        super("Cannot decommission room '" + roomId + "': it still hosts "
              + deviceCount + " sensor device(s). Reassign or remove all devices first.");
        this.deviceCount = deviceCount;
    }

    public int getDeviceCount() { return deviceCount; }
}
