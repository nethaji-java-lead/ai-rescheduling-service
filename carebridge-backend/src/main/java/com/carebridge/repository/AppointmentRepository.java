package com.carebridge.repository;

import com.carebridge.enums.AppointmentStatus;
import com.carebridge.model.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByStatusOrderByCreatedAtDesc(AppointmentStatus status);

    List<Appointment> findByProviderIdAndStatus(String providerId, AppointmentStatus status);

    Optional<Appointment> findBySlotIdAndStatusIn(String slotId, Collection<AppointmentStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") String id);
}
