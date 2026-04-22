package com.smartcampus.api.exception;

public class SensorUnavailableException extends RuntimeException{

    private final String sensorId;
    private final String sensorStatus;


    public SensorUnavailableException(String sensorId, String sensorStatus) {
        super("Sensor '" + sensorId + "' is in " + sensorStatus + " mode, readings cannot be added");
        this.sensorId = sensorId;
        this.sensorStatus = sensorStatus;
    }

    // To return details for exception details
    public String getSensorId() {
        return sensorId;
    }

    public String getSensorStatus() {
        return sensorStatus;
    }
}
