package com.carebridge.service;
import com.carebridge.agent.RescheduleEvent;
import com.carebridge.dto.response.ProviderResponse;
import com.carebridge.dto.request.UpdateProviderStatusRequest;
import com.carebridge.enums.ProviderStatus;
import com.carebridge.enums.TriggerReason;
import com.carebridge.exception.ResourceNotFoundException;
import com.carebridge.repository.ProviderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProviderService {
 private final ProviderRepository providers; private final ApplicationEventPublisher events;
 public ProviderService(ProviderRepository providers, ApplicationEventPublisher events) { this.providers=providers; this.events=events; }
 @Transactional
 public ProviderResponse updateStatus(String id, UpdateProviderStatusRequest request) {
  var provider=providers.findById(id).orElseThrow(()->new ResourceNotFoundException("Provider not found"));
  ProviderStatus old=provider.getStatus(); provider.setStatus(request.status()); providers.save(provider);
  if (request.status()==ProviderStatus.OFFLINE && old!=ProviderStatus.OFFLINE)
   events.publishEvent(new RescheduleEvent(TriggerReason.PROVIDER_OFFLINE, id, null, null));
  return ProviderResponse.from(provider);
 }
}
