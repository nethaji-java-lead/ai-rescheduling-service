package com.example.carebridge.service;
import com.example.carebridge.agent.RescheduleEvent;
import com.example.carebridge.dto.response.ProviderResponse;
import com.example.carebridge.dto.request.UpdateProviderStatusRequest;
import com.example.carebridge.enums.*;
import com.example.carebridge.exception.ResourceNotFoundException;
import com.example.carebridge.repository.ProviderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProviderService {
 private final ProviderRepository providers; private final ApplicationEventPublisher events;
 public ProviderService(ProviderRepository providers, ApplicationEventPublisher events) { this.providers=providers; this.events=events; }
 @Transactional
 public ProviderResponse updateStatus(Long id, UpdateProviderStatusRequest request) {
  var provider=providers.findById(id).orElseThrow(()->new ResourceNotFoundException("Provider not found"));
  ProviderStatus old=provider.getStatus(); provider.setStatus(request.status()); providers.save(provider);
  if (request.status()==ProviderStatus.OFFLINE && old!=ProviderStatus.OFFLINE)
   events.publishEvent(new RescheduleEvent(TriggerReason.PROVIDER_OFFLINE, id, null, null));
  return ProviderResponse.from(provider);
 }
}
