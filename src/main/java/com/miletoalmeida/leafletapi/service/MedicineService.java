package com.miletoalmeida.leafletapi.service;

import com.miletoalmeida.leafletapi.exception.ScrapingException;
import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import com.miletoalmeida.leafletapi.repository.MedicineRepository;
import com.miletoalmeida.leafletapi.service.scraping.AnvisaScrapingService;
import jakarta.persistence.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final AnvisaScrapingService scrapingService;
    private final CacheService cacheService;

    // Cache TTL in minutes
    private static final long CACHE_TTL = 60 * 24; // 24 hours

    @Autowired
    public MedicineService(MedicineRepository medicineRepository,
                           AnvisaScrapingService scrapingService,
                           CacheService cacheService) {
        this.medicineRepository = medicineRepository;
        this.scrapingService = scrapingService;
        this.cacheService = cacheService;
    }

    @Cacheable(value = "searches", key = "#query")
    public List<MedicineDTO> searchMedicines(String query) throws ScrapingException {
        // Check DB cache first
        String cacheKey = "search_" + query.toLowerCase();
        Optional<List> cachedResult = cacheService.getFromCache(cacheKey, "SEARCH", List.class);

        if (cachedResult.isPresent()) {
            return cachedResult.get();
        }

        // If not cached, fetch from Anvisa
        List<MedicineDTO> results = scrapingService.searchMedicines(query);

        // Save results to cache
        if (!results.isEmpty()) {
            cacheService.saveToCache(cacheKey, "SEARCH", results, CACHE_TTL);

            // Also save individual medicines to database
            results.forEach(this::saveMedicineToDb);
        }

        return results;
    }

    @Cacheable(value = "medicines", key = "#registryNumber")
    public Optional<MedicineDTO> getMedicineByRegistryNumber(String registryNumber) throws ScrapingException {
        // Check DB first
        Optional<MedicineDTO> medicineFromDb = medicineRepository.findByRegistryNumber(registryNumber);

        if (medicineFromDb.isPresent()) {
            MedicineDTO medicine = medicineFromDb.get();

            // Check if cache is still valid
            if (medicine.getCacheExpiry().isAfter(LocalDateTime.now())) {
                return Optional.of(convertToDTO(medicine));
            }
        }

        // If not in DB or expired, search from Anvisa
        List<MedicineDTO> searchResults = scrapingService.searchMedicines(registryNumber);

        // Filter for exact registry number match
        Optional<MedicineDTO> medicineDTO = searchResults.stream()
                .filter(med -> registryNumber.equals(med.getRegistryNumber()))
                .findFirst();

        // Save to DB if found
        medicineDTO.ifPresent(this::saveMedicineToDb);

        return medicineDTO;
    }

    private void saveMedicineToDb(MedicineDTO medicineDTO) {
        MedicineDTO medicine = medicineRepository.findByRegistryNumber(medicineDTO.getRegistryNumber())
                .orElse(new MedicineDTO());

        // Update fields
        medicine.setRegistryNumber(medicineDTO.getRegistryNumber());
        medicine.setProductName(medicineDTO.getProductName());
        medicine.setCompany(medicineDTO.getCompany());
        medicine.setActiveIngredient(medicineDTO.getActiveIngredient());
        medicine.setTherapeuticClass(medicineDTO.getTherapeuticClass());
        medicine.setRegulatoryType(medicineDTO.getRegulatoryType());
        medicine.setPresentation(medicineDTO.getPresentation());
        medicine.setLeafletUrl(medicineDTO.getLeafletUrl());

        // Update cache metadata
        medicine.setLastUpdated(LocalDateTime.now());
        medicine.setCacheExpiry(LocalDateTime.now().plusMinutes(CACHE_TTL));

        medicineRepository.save(medicine);
    }

    private MedicineDTO convertToDTO(MedicineDTO medicine) {
        MedicineDTO dto = new MedicineDTO();
        dto.setRegistryNumber(medicine.getRegistryNumber());
        dto.setProductName(medicine.getProductName());
        dto.setCompany(medicine.getCompany());
        dto.setActiveIngredient(medicine.getActiveIngredient());
        dto.setTherapeuticClass(medicine.getTherapeuticClass());
        dto.setRegulatoryType(medicine.getRegulatoryType());
        dto.setPresentation(medicine.getPresentation());
        dto.setLeafletUrl(medicine.getLeafletUrl());
        return dto;
    }
}