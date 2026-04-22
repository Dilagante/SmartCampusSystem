package com.smartcampus.api.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {


    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> error = new LinkedHashMap<>();

        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("message", ex.getMessage());
        error.put("sensorId", ex.getSensorId());
        error.put("sensorState", ex.getSensorStatus());
        error.put("hint", "If the sensor is no longer being in maintenance, change the Sensor Status to ACTIVE");

        return Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();


    }
}
