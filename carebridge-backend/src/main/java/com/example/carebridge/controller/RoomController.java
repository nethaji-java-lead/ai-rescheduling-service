package com.example.carebridge.controller;

import com.example.carebridge.dto.request.UpdateRoomAvailabilityRequest;
import com.example.carebridge.dto.response.RoomResponse;
import com.example.carebridge.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomResponse> list() {
        return service.list();
    }

    @PatchMapping("/{id}/availability")
    public RoomResponse updateAvailability(@PathVariable Long id,
                                           @Valid @RequestBody UpdateRoomAvailabilityRequest request) {
        return service.updateAvailability(id, request.available());
    }
}
