package com.smartcampus.api.model;

public class Sensor {

    private String id;              // Unique identifier - "TEMP-001"
    private String type;            // Category - "Temperature", "Occupancy", "CO2"
    private String status;          // Current state - "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue;    // The most recent measurement recorded
    private String roomId;          // Foreign key linking to the Room where the sensor is located.

    // Empty Constructor
    public Sensor() {
    }

    // Full Constructor
    public Sensor(String id, String type, String status, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.roomId = roomId;
    }

    // Full Constructor without RoomID
    public Sensor(String id, String type, String roomId) {
        this.id = id;
        this.type = type;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
