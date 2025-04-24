package com.miletoalmeida.leafletapi.service;

import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import com.miletoalmeida.leafletapi.exception.ResourceNotFoundException;
import com.miletoalmeida.leafletapi.exception.ScrapingException;
import com.miletoalmeida.leafletapi.service.scraping.AnvisaScrapingService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private static final String CACHE_MEDICINES = "medicines";
    private static final String CACHE_MEDICINE_DETAILS = "medicine_details";
    
    private final AnvisaScrapingService anvisaScrapingService;

    /**
     * Busca medicamentos pelo nome ou princípio ativo.
     *
     * @param query Termo de busca (nome do medicamento ou princípio ativo)
     * @return Lista de medicamentos encontrados
     * @throws IllegalArgumentException se a query estiver vazia
     * @throws ScrapingException se houver erro na consulta à Anvisa
     */
    @Timed(value = "medicine.search", description = "Tempo para buscar medicamentos")
    @Cacheable(value = CACHE_MEDICINES, key = "#query.toLowerCase()", unless = "#result.isEmpty()")
    public List<MedicineDTO> searchMedicines(String query) {
        validateSearchQuery(query);
        
        try {
            log.info("Buscando medicamentos com o termo: {}", query);
            List<MedicineDTO> medicines = anvisaScrapingService.searchMedicines(query);
            
            if (medicines.isEmpty()) {
                log.info("Nenhum medicamento encontrado para a busca: {}", query);
                return Collections.emptyList();
            }
            
            log.info("Encontrados {} medicamentos para a busca: {}", medicines.size(), query);
            return medicines;
        } catch (ScrapingException e) {
            log.error("Erro ao buscar medicamentos: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca um medicamento específico pelo número de registro.
     *
     * @param registryNumber Número de registro do medicamento na Anvisa
     * @return Medicamento encontrado
     * @throws IllegalArgumentException se o número de registro estiver vazio
     * @throws ResourceNotFoundException se o medicamento não for encontrado
     * @throws ScrapingException se houver erro na consulta à Anvisa
     */
    @Timed(value = "medicine.get_details", description = "Tempo para obter detalhes do medicamento")
    @Cacheable(value = CACHE_MEDICINE_DETAILS, key = "#registryNumber")
    public Optional<MedicineDTO> getMedicineByRegistryNumber(String registryNumber) {
        validateRegistryNumber(registryNumber);
        
        try {
            log.info("Buscando medicamento com registro: {}", registryNumber);
            List<MedicineDTO> medicines = anvisaScrapingService.searchMedicines(registryNumber);
            
            Optional<MedicineDTO> medicine = medicines.stream()
                    .filter(m -> registryNumber.equals(m.getRegistryNumber()))
                    .findFirst();
            
            if (medicine.isEmpty()) {
                log.info("Medicamento não encontrado com registro: {}", registryNumber);
            } else {
                log.info("Medicamento encontrado com registro: {}", registryNumber);
            }
            
            return medicine;
        } catch (ScrapingException e) {
            log.error("Erro ao buscar medicamento por registro: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtém a bula de um medicamento pelo número de registro.
     *
     * @param registryNumber Número de registro do medicamento na Anvisa
     * @return Bula do medicamento
     * @throws IllegalArgumentException se o número de registro estiver vazio
     * @throws ResourceNotFoundException se a bula não for encontrada
     * @throws ScrapingException se houver erro na consulta à Anvisa
     */
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