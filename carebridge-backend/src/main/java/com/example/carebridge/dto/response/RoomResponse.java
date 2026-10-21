package com.example.carebridge.dto.response;

import com.example.carebridge.model.Room;

public record RoomResponse(Long id, String name, boolean available) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.isAvailable());
    }
}
