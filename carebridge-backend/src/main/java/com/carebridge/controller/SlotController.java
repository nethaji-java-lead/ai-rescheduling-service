package com.carebridge.controller;

import com.carebridge.dto.request.UpdateSlotStatusRequest;
import com.carebridge.dto.response.SlotResponse;
import com.carebridge.repository.SlotRepository;
import com.carebridge.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {
    private final SlotService service;
    private final SlotRepository repository;

    public SlotController(SlotService service, SlotRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<SlotResponse> list() {
        return repository.findAll().stream().map(SlotResponse::from).toList();
    }

    @PatchMapping("/{id}/status")
    public SlotResponse update(@PathVariable String id, @Valid @RequestBody UpdateSlotStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
