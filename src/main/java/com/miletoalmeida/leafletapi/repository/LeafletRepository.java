package com.miletoalmeida.leafletapi.repository;

import com.miletoalmeida.leafletapi.model.DTO.Leaflet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeafletRepository extends JpaRepository<Leaflet, Long> {
    Optional<Leaflet> findByMedicineRegistryNumber(String registryNumber);

    @Query("SELECT l FROM Leaflet l WHERE l.cacheExpiry < ?1")
    List<Leaflet> findExpiredCache(LocalDateTime now);
}