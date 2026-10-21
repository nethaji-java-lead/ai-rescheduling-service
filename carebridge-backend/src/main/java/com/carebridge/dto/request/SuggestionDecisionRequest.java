package com.carebridge.dto.request;

import com.carebridge.enums.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

public record SuggestionDecisionRequest(@NotNull SuggestionStatus decision) {
}
