package com.miletoalmeida.leafletapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDetail extends MedicineSummary {
    private LocalDate registrationDate;
    private LocalDate expirationDate;
    private String processNumber;
    private String administrationForm;
    private String registerStatus;
    private String packageType;
    private List<String> contraindications;
    private List<String> sideEffects;
    private String dosage;
    private String storage;
}