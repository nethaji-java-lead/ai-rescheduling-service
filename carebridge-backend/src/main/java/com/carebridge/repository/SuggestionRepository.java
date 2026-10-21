package com.carebridge.repository;
import com.carebridge.enums.SuggestionStatus;
import com.carebridge.enums.TriggerReason;
import com.carebridge.model.RescheduleSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SuggestionRepository extends JpaRepository<RescheduleSuggestion, Long> {
    boolean existsByAppointmentIdAndTriggerReasonAndStatus(String appointmentId, TriggerReason reason, SuggestionStatus status);
    List<RescheduleSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    List<RescheduleSuggestion> findByAppointmentIdAndStatus(String appointmentId, SuggestionStatus status);
}
