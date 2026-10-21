package com.example.carebridge.model;

import com.example.carebridge.enums.ProviderSpecialty;
import com.example.carebridge.enums.ProviderStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "providers")
public class Provider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProviderSpecialty specialty;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProviderStatus status = ProviderStatus.AVAILABLE;

    protected Provider() {}
    public Provider(String name, ProviderSpecialty specialty, ProviderStatus status) {
        this.name = name; this.specialty = specialty; this.status = status;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public ProviderSpecialty getSpecialty() { return specialty; }
    public ProviderStatus getStatus() { return status; }
    public void setName(String name) { this.name = name; }
    public void setSpecialty(ProviderSpecialty specialty) { this.specialty = specialty; }
    public void setStatus(ProviderStatus status) { this.status = status; }
}
