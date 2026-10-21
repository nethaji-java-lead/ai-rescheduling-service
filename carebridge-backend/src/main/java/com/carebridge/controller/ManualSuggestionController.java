package com.carebridge.controller;
import com.carebridge.dto.response.SuggestionResponse;
import com.carebridge.service.SuggestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class ManualSuggestionController {
 private final SuggestionService service;
 public ManualSuggestionController(SuggestionService service) { this.service=service; }
 @PostMapping("/appointments/{id}/suggest")
 public List<SuggestionResponse> suggest(@PathVariable String id) { return service.suggest(id); }
}
