package com.miletoalmeida.leafletapi.service;

import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import com.miletoalmeida.leafletapi.exception.ScrapingException;
import com.miletoalmeida.leafletapi.model.Medicine;
import com.miletoalmeida.leafletapi.repository.MedicineRepository;
import com.miletoalmeida.leafletapi.service.scraping.AnvisaScrapingService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private static final String CACHE_MEDICINES = "medicines";
    private static final String CACHE_MEDICINE_DETAILS = "medicine_details";
    private static final int CACHE_DAYS = 7;
    
    private final AnvisaScrapingService anvisaScrapingService;
    private final MedicineRepository medicineRepository;

    @Timed(value = "medicine.search", description = "Tempo para buscar medicamentos")
    @Cacheable(value = CACHE_MEDICINES, key = "#query.toLowerCase()", unless = "#result.isEmpty()")
    public List<MedicineDTO> searchMedicines(String query) {
        validateSearchQuery(query);
        
        try {
            log.info("Buscando medicamentos com o termo: {}", query);
            List<MedicineDTO> scrapedMedicines = anvisaScrapingService.searchMedicines(query);
            
            if (scrapedMedicines.isEmpty()) {
                log.info("Nenhum medicamento encontrado para a busca: {}", query);
                return Collections.emptyList();
            }
            
            // Salva os medicamentos encontrados no banco
            List<Medicine> medicines = scrapedMedicines.stream()
                .map(dto -> {
                    Medicine medicine = dto.toEntity();
                    medicine.setLastUpdated(LocalDateTime.now());
                    medicine.setCachingTime(LocalDateTime.now().plusDays(7));
                    return medicine;
                })
                .collect(Collectors.toList());
            
            medicineRepository.saveAll(medicines);
            
            log.info("Encontrados {} medicamentos para a busca: {}", medicines.size(), query);
            return scrapedMedicines;
        } catch (ScrapingException e) {
            log.error("Erro ao buscar medicamentos: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Timed(value = "medicine.get_details", description = "Tempo para obter detalhes do medicamento")
    @Cacheable(value = CACHE_MEDICINE_DETAILS, key = "#registryNumber")
    public Optional<MedicineDTO> getMedicineByRegistryNumber(String registryNumber) {
        validateRegistryNumber(registryNumber);
        LocalDateTime now = LocalDateTime.now();
        
        try {
            Optional<Medicine> medicineFromDb = medicineRepository.findByRegistryNumber(registryNumber);
            
            if (medicineFromDb.isPresent() && medicineFromDb.get().getCachingTime().isAfter(now)) {
                Medicine medicine = medicineFromDb.get();
                return Optional.of(new MedicineDTO(medicine));
            }
            
            log.info("Buscando medicamento com registro: {}", registryNumber);
            List<MedicineDTO> medicines = Objects.requireNonNull(
                anvisaScrapingService.searchMedicines(registryNumber)
            );
            
            Optional<MedicineDTO> medicineDto = medicines.stream()
                    .filter(m -> registryNumber.equals(m.getRegistryNumber()))
                    .findFirst();
            
            if (medicineDto.isPresent()) {
                Medicine medicine = medicineDto.get().toEntity();
                medicine.setLastUpdated(now);
                medicine.setCachingTime(now.plusDays(CACHE_DAYS));
                medicineRepository.save(medicine);
            }
            
            return medicineDto;
            
        } catch (ScrapingException e) {
            log.error("Erro ao buscar medicamento por registro: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar medicamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar a busca do medicamento", e);
        }
    }

    @Timed(value = "medicine.get_leaflet", description = "Tempo para obter bula do medicamento")
    @Cacheable(value = "leaflets", key = "#registryNumber")
    public LeafletDTO getLeaflet(String registryNumber) {
        validateRegistryNumber(registryNumber);
        
        try {
            log.info("Buscando bula do medicamento com registro: {}", registryNumber);
            return anvisaScrapingService.getLeaflet(registryNumber);
        } catch (ScrapingException e) {
            log.error("Erro ao buscar bula do medicamento: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateSearchQuery(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("O termo de busca não pode estar vazio");
        }
        if (query.length() < 3) {
            throw new IllegalArgumentException("O termo de busca deve ter pelo menos 3 caracteres");
        }
    }

    private void validateRegistryNumber(String registryNumber) {
        if (!StringUtils.hasText(registryNumber)) {
            throw new IllegalArgumentException("O número de registro não pode estar vazio");
        }
        if (!registryNumber.matches("\\d+")) {
            throw new IllegalArgumentException("O número de registro deve conter apenas números");
        }
    }
}