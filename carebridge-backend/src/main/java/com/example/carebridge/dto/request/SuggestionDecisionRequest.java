package com.example.carebridge.dto.request;
import com.example.carebridge.enums.SuggestionStatus;
import jakarta.validation.constraints.NotNull;
public record SuggestionDecisionRequest(@NotNull SuggestionStatus decision) {}
