package com.example.carebridge.controller;
import com.example.carebridge.dto.request.CreateAppointmentRequest;
import com.example.carebridge.dto.response.AppointmentResponse;
import com.example.carebridge.enums.AppointmentStatus;
import com.example.carebridge.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/appointments")
public class AppointmentController {
 private final AppointmentService service;
 public AppointmentController(AppointmentService service) { this.service=service; }
 @PostMapping public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
  return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
 }
 @GetMapping public List<AppointmentResponse> list(@RequestParam(required=false) AppointmentStatus status) { return service.list(status); }
}
