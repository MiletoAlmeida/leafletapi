package com.miletoalmeida.leafletapi.dto;

import com.miletoalmeida.leafletapi.model.Medicine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private String id;
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
    
    // Construtor para converter de Medicine para DTO
    public MedicineDTO(Medicine medicine) {
        this.id = medicine.getId();
        this.processNumber = medicine.getProcessNumber();
        this.registryNumber = medicine.getRegistryNumber();
        this.productName = medicine.getProductName();
        this.company = medicine.getCompany();
        this.cnpj = medicine.getCnpj();
        this.activeIngredient = medicine.getActiveIngredient();
        this.therapeuticClass = medicine.getTherapeuticClass();
        this.regulatoryType = medicine.getRegulatoryType();
        this.presentation = medicine.getPresentation();
        this.leafletUrl = medicine.getLeafletUrl();
    }
    
    // Método para converter de DTO para Medicine
    public Medicine toEntity() {
        Medicine medicine = new Medicine();
        medicine.setId(this.id);
        medicine.setProcessNumber(this.processNumber);
        medicine.setRegistryNumber(this.registryNumber);
        medicine.setProductName(this.productName);
        medicine.setCompany(this.company);
        medicine.setCnpj(this.cnpj);
        medicine.setActiveIngredient(this.activeIngredient);
        medicine.setTherapeuticClass(this.therapeuticClass);
        medicine.setRegulatoryType(this.regulatoryType);
        medicine.setPresentation(this.presentation);
        medicine.setLeafletUrl(this.leafletUrl);
        return medicine;
    }
}