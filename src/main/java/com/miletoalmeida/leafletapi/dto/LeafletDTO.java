package com.miletoalmeida.leafletapi.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeafletDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicineRegistryNumber;

    @Column(columnDefinition = "TEXT")
    private String patientLeaflet;

    @Column(columnDefinition = "TEXT")
    private String professionalLeaflet;

    private LocalDateTime lastUpdated;
    private LocalDateTime cacheExpiry;
}
