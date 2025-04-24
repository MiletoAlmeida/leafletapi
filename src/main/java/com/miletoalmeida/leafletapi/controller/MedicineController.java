package com.miletoalmeida.leafletapi.controller;

import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import com.miletoalmeida.leafletapi.dto.ResponseDTO;
import com.miletoalmeida.leafletapi.exception.ScrapingException;
import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import com.miletoalmeida.leafletapi.service.LeafletService;
import com.miletoalmeida.leafletapi.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MedicineController {

    private final MedicineService medicineService;
    private final LeafletService leafletService;

    @Autowired
    public MedicineController(MedicineService medicineService, LeafletService leafletService) {
        this.medicineService = medicineService;
        this.leafletService = leafletService;
    }

    @GetMapping("/medicines/search")
    public ResponseEntity<ResponseDTO<List<MedicineDTO>>> searchMedicines(@RequestParam String query) {
        try {
            List<MedicineDTO> medicines = medicineService.searchMedicines(query);

            if (medicines.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.success(
                    medicines, 
                    "Nenhum medicamento encontrado para a busca: " + query
                ));
            }

            return ResponseEntity.ok(ResponseDTO.success(
                medicines, 
                "Medicamentos encontrados com sucesso"
            ));
        } catch (ScrapingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar medicamentos: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/medicines/{registryNumber}")
    public ResponseEntity<ResponseDTO<MedicineDTO>> getMedicineByRegistryNumber(@PathVariable String registryNumber) {
        try {
            Optional<MedicineDTO> medicine = medicineService.getMedicineByRegistryNumber(registryNumber);

            if (medicine.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.notFound(
                        "Medicamento não encontrado com número de registro: " + registryNumber
                    ));
            }

            return ResponseEntity.ok(ResponseDTO.success(
                medicine.get(), 
                "Medicamento encontrado com sucesso"
            ));
        } catch (ScrapingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar medicamento: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/leaflets/{registryNumber}")
    public ResponseEntity<ResponseDTO<LeafletDTO>> getLeafletByRegistryNumber(@PathVariable String registryNumber) {
        try {
            Optional<LeafletDTO> leaflet = leafletService.getLeafletByRegistryNumber(registryNumber);

            if (leaflet.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.notFound(
                        "Bula não encontrada para o medicamento com número de registro: " + registryNumber
                    ));
            }

            return ResponseEntity.ok(ResponseDTO.success(
                leaflet.get(), 
                "Bula encontrada com sucesso"
            ));
        } catch (ScrapingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar bula: " + e.getMessage()
                ));
        }
    }
}