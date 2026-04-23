package com.smartcampus.api.resource;


import com.smartcampus.api.dao.DataStore;
import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        // Case 4 - Invalid Status
        if (!sensor.getStatus().equalsIgnoreCase("ACTIVE") && !sensor.getStatus().equalsIgnoreCase("MAINTENANCE")
                && !sensor.getStatus().equalsIgnoreCase("OFFLINE")) {
            return Response.
                    status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Status should be 'ACTIVE', 'MAINTENANCE', or 'OFFLINE'\"}")
                    .build();
        }

        // If not problems...
        store.saveSensor(sensor);

        store.findRoomById(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response
                .status(Response.Status.CREATED)
                .entity(sensor)
                .build();

    }

    // GET - api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        Sensor sensor = store.findSensorById(sensorId);

        if (sensor == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(sensor).build();
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

    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existing = store.findSensorById(sensorId);
        if (existing == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        if (updatedSensor.getType() != null && !updatedSensor.getType().isBlank()) {
            existing.setType(updatedSensor.getType());
        }

        // Validate status is one of the allowed values
        if (updatedSensor.getStatus() != null && !updatedSensor.getStatus().isBlank()) {
            String newStatus = updatedSensor.getStatus().toUpperCase();
            if (!newStatus.equals("ACTIVE") && !newStatus.equals("MAINTENANCE")
                    && !newStatus.equals("OFFLINE")) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("status", 400);
                error.put("error", "Bad Request");
                error.put("message", "Status must be ACTIVE, MAINTENANCE, or OFFLINE");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
            existing.setStatus(newStatus);
        }

        // Changing a sensor's room requires updating both the old and new room's sensorIds
        if (updatedSensor.getRoomId() != null && !updatedSensor.getRoomId().isBlank()
                && !updatedSensor.getRoomId().equals(existing.getRoomId())) {

            if (store.findRoomById(updatedSensor.getRoomId()) == null) {
                throw new LinkedResourceNotFoundException("Room", updatedSensor.getRoomId());
            }

            // Remove from old room
            Room oldRoom = store.findRoomById(existing.getRoomId());
            if (oldRoom != null) {
                oldRoom.getSensorIds().remove(existing.getId());
            }

            // Add to new room
            store.findRoomById(updatedSensor.getRoomId()).getSensorIds().add(existing.getId());
            existing.setRoomId(updatedSensor.getRoomId());
        }

        return Response.ok(existing).build();
    }





}
