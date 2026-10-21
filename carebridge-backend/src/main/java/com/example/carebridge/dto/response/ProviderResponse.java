package com.example.carebridge.dto.response;
import com.example.carebridge.enums.*;
import com.example.carebridge.model.Provider;
public record ProviderResponse(Long id, String name, ProviderSpecialty specialty, ProviderStatus status) {
 public static ProviderResponse from(Provider p) { return new ProviderResponse(p.getId(), p.getName(), p.getSpecialty(), p.getStatus()); }
}
