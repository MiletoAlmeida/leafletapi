package com.miletoalmeida.leafletapi.service;

import com.miletoalmeida.leafletapi.exception.ScrapingException;
import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import com.miletoalmeida.leafletapi.repository.LeafletRepository;
import com.miletoalmeida.leafletapi.service.scraping.AnvisaScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeafletService {

    private final LeafletRepository leafletRepository;
    private final AnvisaScrapingService scrapingService;

    // Cache TTL in minutes
    private static final long CACHE_TTL = 60 * 24 * 7; // 7 days

    @Autowired
    public LeafletService(LeafletRepository leafletRepository,
                          AnvisaScrapingService scrapingService) {
        this.leafletRepository = leafletRepository;
        this.scrapingService = scrapingService;
    }

    @Cacheable(value = "leaflets", key = "#registryNumber")
    public Optional<LeafletDTO> getLeafletByRegistryNumber(String registryNumber) throws ScrapingException {
        // Check DB first
        Optional<LeafletDTO> leafletFromDb = leafletRepository.findByMedicineRegistryNumber(registryNumber);

        if (leafletFromDb.isPresent()) {
            LeafletDTO leaflet = leafletFromDb.get();

            // Check if cache is still valid
            if (leaflet.getCacheExpiry().isAfter(LocalDateTime.now())) {
                return Optional.of(convertToDTO(leaflet));
            }
        }

        // If not in DB or expired, fetch from Anvisa
        LeafletDTO leafletDTO = scrapingService.getLeaflet(registryNumber);

        // Save to DB
        saveLeafletToDb(registryNumber, leafletDTO);

        return Optional.of(leafletDTO);
    }

    private void saveLeafletToDb(String registryNumber, LeafletDTO leafletDTO) {
        LeafletDTO leaflet = leafletRepository.findByMedicineRegistryNumber(registryNumber)
                .orElse(new LeafletDTO());

        // Update fields
        leaflet.setMedicineRegistryNumber(registryNumber);
        leaflet.setPatientLeaflet(leafletDTO.getPatientLeaflet());
        leaflet.setProfessionalLeaflet(leafletDTO.getProfessionalLeaflet());

        // Update cache metadata
        leaflet.setLastUpdated(LocalDateTime.now());
        leaflet.setCacheExpiry(LocalDateTime.now().plusMinutes(CACHE_TTL));

        leafletRepository.save(leaflet);
    }

    private LeafletDTO convertToDTO(LeafletDTO leaflet) {
        LeafletDTO dto = new LeafletDTO();
        dto.setPatientLeaflet(leaflet.getPatientLeaflet());
        dto.setProfessionalLeaflet(leaflet.getProfessionalLeaflet());
        return dto;
    }
}