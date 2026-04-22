package com.smartcampus.api.resource;


import com.smartcampus.api.dao.DataStore;
import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Get DataStore Instance
    private final DataStore store = DataStore.getInstance();

    // GET - api/v1/sensors OR
    // GET - api/v1/sensors?type={type}
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = store.getAllSensors();

        if (type != null && !type.isBlank()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(java.util.stream.Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    // POST - api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {

        // Case 1 - Empty Sensor ID
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.
                    status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Sensor ID is required\"}")
                    .build();
        }

        // Case 2 - Duplicate Sensor ID
        if (store.findSensorById(sensor.getId()) != null) {
            return Response.
                    status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"A Sensor with ID '" + sensor.getId() + "' already exists\"}")
                    .build();
        }

        // Case 3 - Invalid Room ID
        if (sensor.getRoomId() == null || store.findRoomById(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // If not problems...
        store.saveSensor(sensor);

        store.findRoomById(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response
                .status(Response.Status.CREATED)
                .entity(sensor)
                .build();

    }

    // Sub-resource locator - api/v1/sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.findSensorById(sensorId);

        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found");
        }

        // Pass resolved sensor to the sub-resource
        return new SensorReadingResource(sensor);
    }





}
