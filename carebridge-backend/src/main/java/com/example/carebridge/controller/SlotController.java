package com.example.carebridge.controller;
import com.example.carebridge.dto.request.UpdateSlotStatusRequest;
import com.example.carebridge.dto.response.SlotResponse;
import com.example.carebridge.repository.SlotRepository;
import com.example.carebridge.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/slots")
public class SlotController {
 private final SlotService service; private final SlotRepository repository;
 public SlotController(SlotService service, SlotRepository repository) { this.service=service; this.repository=repository; }
 @GetMapping public List<SlotResponse> list() { return repository.findAll().stream().map(SlotResponse::from).toList(); }
 @PatchMapping("/{id}/status") public SlotResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSlotStatusRequest request) { return service.updateStatus(id, request); }
}
