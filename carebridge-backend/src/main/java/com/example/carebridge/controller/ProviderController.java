package com.example.carebridge.controller;
import com.example.carebridge.dto.request.UpdateProviderStatusRequest;
import com.example.carebridge.dto.response.ProviderResponse;
import com.example.carebridge.repository.ProviderRepository;
import com.example.carebridge.service.ProviderService;
import com.example.carebridge.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/providers")
public class ProviderController {
 private final ProviderService service; private final ProviderRepository repository;
 public ProviderController(ProviderService service, ProviderRepository repository) { this.service=service; this.repository=repository; }
 @GetMapping public List<ProviderResponse> list() { return repository.findAll().stream().map(ProviderResponse::from).toList(); }
 @PatchMapping("/{id}/status") public ProviderResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProviderStatusRequest request) { return service.updateStatus(id, request); }
}
