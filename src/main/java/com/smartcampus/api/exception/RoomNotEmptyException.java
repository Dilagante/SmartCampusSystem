package com.smartcampus.api.exception;

public class RoomNotEmptyException extends RuntimeException{

    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' still has sensors assigned to it...");
        this.roomId = roomId;
    }

    // To return room ID for exception details
    public String getRoomId() {
        return roomId;
    }

}
