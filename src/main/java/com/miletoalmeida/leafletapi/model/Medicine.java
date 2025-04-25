package com.miletoalmeida.leafletapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "process_number")
    private String processNumber;

    @Column(name = "registry_number", unique = true)
    private String registryNumber;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "company")
    private String company;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "active_ingredient")
    private String activeIngredient;

    @Column(name = "therapeutic_class")
    private String therapeuticClass;

    @Column(name = "regulatory_type")
    private String regulatoryType;

    @Column(name = "presentation")
    private String presentation;

    @Column(name = "leaflet_url")
    private String leafletUrl;

    @Column(name = "cache_expiry")
    private LocalDateTime cachingTime;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}