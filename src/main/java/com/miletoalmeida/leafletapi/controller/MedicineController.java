package com.miletoalmeida.leafletapi.controller;

import com.miletoalmeida.leafletapi.model.MedicineDetail;
import com.miletoalmeida.leafletapi.model.MedicineSummary;
import com.miletoalmeida.leafletapi.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@Tag(name = "Medicine API", description = "Endpoints para buscar e recuperar informações de medicamentos e suas bulas")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Operation(summary = "Buscar medicamentos por nome ou fabricante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medicamentos encontrados"),
            @ApiResponse(responseCode = "404", description = "Nenhum medicamento encontrado")
    })
    @GetMapping
    public ResponseEntity<List<MedicineSummary>> getMedicines(
            @Parameter(description = "Nome do medicamento") @RequestParam(required = false) String name,
            @Parameter(description = "Nome do fabricante") @RequestParam(required = false) String manufacturer) {

        List<MedicineSummary> results;

        if (name != null && !name.isEmpty()) {
            results = medicineService.findByName(name);
        } else if (manufacturer != null && !manufacturer.isEmpty()) {
            results = medicineService.findByManufacturer(manufacturer);
        } else {
            return ResponseEntity.badRequest().build();
        }

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Buscar informações detalhadas de um medicamento por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medicamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Medicamento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MedicineDetail> getMedicineDetails(@PathVariable String id) {
        MedicineDetail medicine = medicineService.findDetailById(id);

        if (medicine == null || medicine.getId() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(medicine);
    }

    @Operation(summary = "Buscar a bula de um medicamento específico")
    @GetMapping("/{id}/leaflet")
    public ResponseEntity<String> getMedicineLeaflet(@PathVariable String id) {
        String leafletUrl = medicineService.getLeafletUrl(id);

        if (leafletUrl == null || leafletUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(leafletUrl);
    }
}