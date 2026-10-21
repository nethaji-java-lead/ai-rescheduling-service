package com.example.carebridge.controller;
import com.example.carebridge.dto.request.SuggestionDecisionRequest;
import com.example.carebridge.dto.response.SuggestionResponse;
import com.example.carebridge.enums.SuggestionStatus;
import com.example.carebridge.repository.SuggestionRepository;
import com.example.carebridge.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/suggestions")
public class SuggestionController {
 private final SuggestionService service; private final SuggestionRepository repository;
 public SuggestionController(SuggestionService service, SuggestionRepository repository) { this.service=service; this.repository=repository; }
 @GetMapping public List<SuggestionResponse> list(@RequestParam(defaultValue="PENDING") SuggestionStatus status) {
  return repository.findByStatusOrderByCreatedAtDesc(status).stream().map(SuggestionResponse::from).toList();
 }
 @PatchMapping("/{id}") public SuggestionResponse decide(@PathVariable Long id, @Valid @RequestBody SuggestionDecisionRequest request) {
  return service.decide(id, request.decision());
 }
}
