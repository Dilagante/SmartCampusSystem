package com.smartcampus.api.resource;

import com.smartcampus.api.dao.DataStore;
import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // Get DataStore Instance
    private final DataStore store = DataStore.getInstance();

    // GET - /api/v1/rooms
    @GET
    public Response getAllRooms() {
        List<Room> rooms = store.getAllRooms();
        return Response.ok(rooms).build();
    }

    // POST - /api/v1/rooms
    @POST
    public Response createRoom(Room room) {
        // Case 1 - Empty Room ID
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.
                    status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required\"}")
                    .build();
        }

        // Case 2 - Empty Room Name
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.
                    status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room name is required\"}")
                    .build();
        }

        // Case 3 - Duplicate ID
        if (store.findRoomById(room.getId()) != null) {
            return Response.
                    status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"A room with ID '" + room.getId() + "' already exists\"}")
                    .build();
        }

        // Good Case!
        store.saveRoom(room);

        return Response.
                status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // GET - /api/v1/rooms/{roomId}
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        // Retrieve Room from Store
        Room room = store.findRoomById(roomId);

        // Bad Case - No room found
        if (room == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room with ID '" + roomId + "' not found\"}")
                    .build();
        }

        // Good Case - we found le room
        return Response.ok(room).build();

    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        // Check if the room exists
        Room room = store.findRoomById(roomId);
        if (room == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Room with ID '" + roomId + "' not found");
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        // Check if sensors are assigned
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        // If safe to delete...
        store.deleteRoom(roomId);

        return Response.noContent().build();

    }

}
