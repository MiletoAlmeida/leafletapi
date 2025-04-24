package com.miletoalmeida.leafletapi.DTO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String processNumber;
    private String registryNumber;
    private String productName;
    private String company;
    private String cnpj;
    private String activeIngredient;
    private String therapeuticClass;
    private String regulatoryType;
    private String presentation;

    @Column(length = 1000)
    private String leafletUrl;

    private LocalDateTime lastUpdated;
    private LocalDateTime cacheExpiry;
}