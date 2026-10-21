package com.carebridge.repository;
import com.carebridge.enums.AppointmentStatus;
import com.carebridge.model.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByStatusOrderByCreatedAtDesc(AppointmentStatus status);
    List<Appointment> findByProviderIdAndStatus(String providerId, AppointmentStatus status);
    Optional<Appointment> findBySlotIdAndStatusIn(String slotId, Collection<AppointmentStatus> statuses);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") String id);
}
