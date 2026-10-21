package com.carebridge.model;

import com.carebridge.enums.SuggestionStatus;
import com.carebridge.enums.TriggerReason;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reschedule_suggestions", indexes = {
        @Index(name = "idx_suggestion_appointment_status", columnList = "appointment_id,status"),
        @Index(name = "idx_suggestion_trigger", columnList = "trigger_reason,status")
})
public class RescheduleSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Appointment appointment;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Slot recommendedSlot;
    @ManyToOne(fetch = FetchType.EAGER)
    private Provider alternateProvider;
    @Column(nullable = false)
    private double confidence;
    @Column(nullable = false, length = 1000)
    private String reasoning;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status = SuggestionStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_reason", nullable = false)
    private TriggerReason triggerReason;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected RescheduleSuggestion() {
    }

    public RescheduleSuggestion(Appointment appointment, Slot recommendedSlot, Provider alternateProvider,
                                double confidence, String reasoning, TriggerReason triggerReason) {
        this.appointment = appointment;
        this.recommendedSlot = recommendedSlot;
        this.alternateProvider = alternateProvider;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.triggerReason = triggerReason;
    }

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public Slot getRecommendedSlot() {
        return recommendedSlot;
    }

    public Provider getAlternateProvider() {
        return alternateProvider;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    public TriggerReason getTriggerReason() {
        return triggerReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
