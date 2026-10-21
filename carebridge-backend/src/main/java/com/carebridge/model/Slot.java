package com.carebridge.model;

import com.carebridge.enums.SlotStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slots", indexes = @Index(name = "idx_slot_time_status", columnList = "start_time,status"))
public class Slot {
    @Id
    private String id;
    @Column(name = "start_time", nullable = false) private LocalDateTime startTime;
    @Column(nullable = false) private int durationMinutes;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Provider provider;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Room room;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SlotStatus status = SlotStatus.OPEN;
    @Version private long version;

    protected Slot() {}
    public Slot(LocalDateTime startTime, int durationMinutes, Provider provider, Room room, SlotStatus status) {
        this.startTime = startTime; this.durationMinutes = durationMinutes; this.provider = provider; this.room = room; this.status = status;
    }
    public String getId() { return id; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public Provider getProvider() { return provider; }
    public Room getRoom() { return room; }
    public SlotStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public void setStatus(SlotStatus status) { this.status = status; }
    public void setProvider(Provider provider) { this.provider = provider; }
}
