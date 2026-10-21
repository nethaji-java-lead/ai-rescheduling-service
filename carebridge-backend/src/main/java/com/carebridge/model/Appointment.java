package com.carebridge.model;

import com.carebridge.enums.AppointmentStatus;
import com.carebridge.enums.AppointmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    private String id;
    @Column(nullable = false) private String patientName;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AppointmentType type;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Provider provider;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Slot slot;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AppointmentStatus status = AppointmentStatus.CONFIRMED;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @Version private long version;

    protected Appointment() {}
    @PrePersist
    private void assignIdIfMissing() {
        if (id == null) id = "APT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    public Appointment(String patientName, AppointmentType type, Provider provider, Slot slot) {
        this.patientName = patientName; this.type = type; this.provider = provider; this.slot = slot;
        this.status = AppointmentStatus.CONFIRMED; this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt;
    }
    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public AppointmentType getType() { return type; }
    public Provider getProvider() { return provider; }
    public Slot getSlot() { return slot; }
    public AppointmentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void markReschedulePending() {
        if (status != AppointmentStatus.CONFIRMED) throw new IllegalStateException("Only confirmed appointments can enter rescheduling");
        status = AppointmentStatus.RESCHEDULE_PENDING; updatedAt = LocalDateTime.now();
    }
    public void completeReschedule(Provider provider, Slot slot) {
        if (status != AppointmentStatus.RESCHEDULE_PENDING) throw new IllegalStateException("Appointment is not pending rescheduling");
        this.provider = provider; this.slot = slot; this.status = AppointmentStatus.RESCHEDULED; this.updatedAt = LocalDateTime.now();
    }
}
