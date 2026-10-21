package com.example.carebridge.scheduling;
import com.example.carebridge.enums.TriggerReason;
import com.example.carebridge.model.*;
import java.util.List;
public interface SchedulingEngine {
 List<SchedulingRecommendation> recommend(Appointment appointment, List<Slot> openSlots, List<Provider> providers, TriggerReason reason);
}
