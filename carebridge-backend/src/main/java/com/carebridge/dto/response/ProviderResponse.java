package com.carebridge.dto.response;

import com.carebridge.enums.ProviderSpecialty;
import com.carebridge.enums.ProviderStatus;
import com.carebridge.model.Provider;

public record ProviderResponse(String id, String name, ProviderSpecialty specialty, ProviderStatus status) {
    public static ProviderResponse from(Provider p) {
        return new ProviderResponse(p.getId(), p.getName(), p.getSpecialty(), p.getStatus());
    }
}
