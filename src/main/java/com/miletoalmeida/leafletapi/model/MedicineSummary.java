package com.miletoalmeida.leafletapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineSummary {
    private String id;
    private String name;
    private String manufacturer;
    private String regulatoryCategory;
    private String registrationNumber;
    private String activeIngredient;
    private String therapeuticClass;
    private String pdfUrl;
    private String presentation;
}