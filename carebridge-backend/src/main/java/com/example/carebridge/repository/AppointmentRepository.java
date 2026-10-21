package com.example.carebridge.repository;
import com.example.carebridge.enums.AppointmentStatus;
import com.example.carebridge.model.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStatusOrderByCreatedAtDesc(AppointmentStatus status);
    List<Appointment> findByProviderIdAndStatus(Long providerId, AppointmentStatus status);
    List<Appointment> findByProviderIdAndStatusInOrderByIdAsc(Long providerId, Collection<AppointmentStatus> statuses);
    Optional<Appointment> findBySlotIdAndStatusIn(Long slotId, Collection<AppointmentStatus> statuses);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") Long id);
}
