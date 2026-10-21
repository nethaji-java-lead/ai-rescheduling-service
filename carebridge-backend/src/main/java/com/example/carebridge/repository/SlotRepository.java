package com.example.carebridge.repository;
import com.example.carebridge.enums.SlotStatus;
import com.example.carebridge.model.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByStatusOrderByStartTimeAsc(SlotStatus status);
    List<Slot> findByProviderIdAndStatusOrderByStartTimeAsc(Long providerId, SlotStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}
