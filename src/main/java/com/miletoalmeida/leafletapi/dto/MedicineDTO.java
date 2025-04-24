package com.miletoalmeida.leafletapi.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
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
    private String leafletUrl;
}