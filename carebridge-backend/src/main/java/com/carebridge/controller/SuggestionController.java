package com.carebridge.controller;
import com.carebridge.dto.request.SuggestionDecisionRequest;
import com.carebridge.dto.response.SuggestionResponse;
import com.carebridge.enums.SuggestionStatus;
import com.carebridge.repository.SuggestionRepository;
import com.carebridge.service.SuggestionService;
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
