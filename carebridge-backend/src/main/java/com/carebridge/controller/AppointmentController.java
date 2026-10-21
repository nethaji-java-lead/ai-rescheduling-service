
package com.carebridge.controller;

import com.carebridge.dto.request.CreateAppointmentRequest;
import com.carebridge.dto.response.AppointmentResponse;
import com.carebridge.enums.AppointmentStatus;
import com.carebridge.service.AppointmentService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

 private final AppointmentService service;

 public AppointmentController(AppointmentService service) {
  this.service = service;
 }

 @PostMapping
 public ResponseEntity<AppointmentResponse> create(
         @Valid @RequestBody CreateAppointmentRequest request) {
  return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(service.create(request));
 }

 @GetMapping
 public List<AppointmentResponse> list(
         @RequestParam(name = "status", required = false)
         AppointmentStatus status) {
  return service.list(status);
 }
}
