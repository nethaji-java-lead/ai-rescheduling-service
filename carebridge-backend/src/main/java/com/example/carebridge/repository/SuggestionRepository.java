package com.example.carebridge.repository;
import com.example.carebridge.enums.*;
import com.example.carebridge.model.RescheduleSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SuggestionRepository extends JpaRepository<RescheduleSuggestion, Long> {
    boolean existsByAppointmentIdAndTriggerReasonAndStatus(Long appointmentId, TriggerReason reason, SuggestionStatus status);
    List<RescheduleSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    List<RescheduleSuggestion> findByAppointmentIdAndStatus(Long appointmentId, SuggestionStatus status);
}
