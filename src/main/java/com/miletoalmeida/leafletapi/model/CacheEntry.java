package com.miletoalmeida.leafletapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cacheKey;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String cacheValue;

    private String type; // SEARCH, MEDICINE, LEAFLET

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}