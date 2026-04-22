package com.smartcampus.api.resource;

import com.smartcampus.api.dao.DataStore;
import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final DataStore store = DataStore.getInstance();
    private final Sensor sensor;

    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    // GET - api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        List<SensorReading> readings = store.getReadingsForSensor(sensor.getId());
        return Response.ok(readings).build();
    }

    // POST - api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {

        // If Sensor is in MAINTENANCE mode
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensor.getId(), sensor.getStatus());
        }

        // Validate the presence of a reading
        if (reading == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status",400);
            error.put("error", "Bad Request");
            error.put("message", "Reading body is required");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }

        // If ok, generate UUID and timestamp if empty
        String id = (reading.getId() != null && !reading.getId().isBlank())
                ? reading.getId()
                : UUID.randomUUID().toString();

        long timestamp = (reading.getTimestamp() != 0)
                ? reading.getTimestamp()
                : System.currentTimeMillis();

        // Create SensorReading
        SensorReading newReading = new SensorReading(id, timestamp, reading.getValue());

        store.addReading(sensor.getId(), newReading);

        // Update sensor's current value
        sensor.setCurrentValue(newReading.getValue());

        // Build Response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Reading recorded successfully");
        response.put("sensorId", sensor.getId());
        response.put("reading", newReading);
        response.put("sensorCurrentValue", sensor.getCurrentValue());

        return Response
                .status(Response.Status.CREATED)
                .entity(response)
                .build();

    }
}
