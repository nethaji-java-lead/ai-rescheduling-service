package com.carebridge.scheduling;

import com.carebridge.enums.AppointmentType;
import com.carebridge.enums.ProviderStatus;
import com.carebridge.enums.SlotStatus;
import com.carebridge.enums.TriggerReason;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.Slot;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("ruleBasedSchedulingStrategy")
public class RuleBasedSchedulingStrategy implements SchedulingStrategy {
    @Override
    public List<SchedulingRecommendation> recommend(Appointment appointment, List<Slot> openSlots, List<Provider> providers, TriggerReason reason) {
        Map<String, Provider> providerMap = providers.stream().collect(Collectors.toMap(Provider::getId, p -> p));
        return openSlots.stream()
                .filter(s -> s.getStatus() == SlotStatus.OPEN && s.getRoom().isAvailable())
                .filter(s -> s.getProvider().getStatus() == ProviderStatus.AVAILABLE)
                .filter(s -> !s.getStartTime().isBefore(appointment.getSlot().getStartTime()))
                .filter(s -> s.getProvider().getId().equals(appointment.getProvider().getId())
                        || s.getProvider().getSpecialty() == appointment.getProvider().getSpecialty())
                .sorted(Comparator
                        .comparing((Slot s) -> !s.getProvider().getId().equals(appointment.getProvider().getId()))
                        .thenComparing(s -> appointment.getType() == AppointmentType.URGENT
                                ? Math.abs(Duration.between(appointment.getSlot().getStartTime(), s.getStartTime()).toMinutes())
                                : Duration.between(appointment.getSlot().getStartTime(), s.getStartTime()).toMinutes())
                        .thenComparing(Slot::getStartTime))
                .map(s -> {
                    boolean same = s.getProvider().getId().equals(appointment.getProvider().getId());
                    return new SchedulingRecommendation(s.getId(), same ? null : s.getProvider().getId(),
                            same ? 0.92 : 0.78,
                            same ? "The nearest suitable open slot with your current provider is available."
                                    : "Your current provider is unavailable, so this slot is with an available provider of the same specialty.");
                }).limit(5).toList();
    }
}
