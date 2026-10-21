package com.carebridge.controller;
import com.carebridge.dto.request.UpdateProviderStatusRequest;
import com.carebridge.dto.response.ProviderResponse;
import com.carebridge.repository.ProviderRepository;
import com.carebridge.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/providers")
public class ProviderController {
 private final ProviderService service; private final ProviderRepository repository;
 public ProviderController(ProviderService service, ProviderRepository repository) { this.service=service; this.repository=repository; }
 @GetMapping public List<ProviderResponse> list() { return repository.findAll().stream().map(ProviderResponse::from).toList(); }
 @PatchMapping("/{id}/status") public ProviderResponse update(@PathVariable String id, @Valid @RequestBody UpdateProviderStatusRequest request) { return service.updateStatus(id, request); }
}
