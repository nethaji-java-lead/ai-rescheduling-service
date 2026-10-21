package com.carebridge.scheduling;
import com.carebridge.enums.TriggerReason;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.Slot;
import java.util.List;
public interface SchedulingEngine {
 List<SchedulingRecommendation> recommend(Appointment appointment, List<Slot> openSlots, List<Provider> providers, TriggerReason reason);
}
