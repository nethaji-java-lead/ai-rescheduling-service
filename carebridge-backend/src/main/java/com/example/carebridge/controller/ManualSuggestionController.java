package com.example.carebridge.controller;
import com.example.carebridge.dto.response.SuggestionResponse;
import com.example.carebridge.service.SuggestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class ManualSuggestionController {
 private final SuggestionService service;
 public ManualSuggestionController(SuggestionService service) { this.service=service; }
 @PostMapping("/appointments/{id}/suggest")
 public List<SuggestionResponse> suggest(@PathVariable Long id) { return service.suggest(id); }
}
