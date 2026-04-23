package uk.ac.campus.domain;

/**
 * Represents an IoT sensor device deployed in a campus room.
 * Holds the most recently recorded measurement and links to its host room.
 */
public class Sensor {

    /** Valid operational states for a sensor device. */
    public enum DeviceStatus {
        ACTIVE, MAINTENANCE, OFFLINE;

        public static boolean isValid(String value) {
            if (value == null) return false;
            for (DeviceStatus s : values()) {
                if (s.name().equalsIgnoreCase(value)) return true;
            }
            return false;
        }
    }

    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;

    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public boolean isUnderMaintenance() {
        return DeviceStatus.MAINTENANCE.name().equalsIgnoreCase(this.status);
    }

    public boolean isOfflineOrMaintenance() {
        return DeviceStatus.OFFLINE.name().equalsIgnoreCase(this.status) ||
               DeviceStatus.MAINTENANCE.name().equalsIgnoreCase(this.status);
    }

    public void updateLatestReading(double latestValue) {
        this.currentValue = latestValue;
    }

    // --- Getters / Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}
