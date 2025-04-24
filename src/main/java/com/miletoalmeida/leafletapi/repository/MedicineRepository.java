package com.miletoalmeida.leafletapi.repository;

import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<MedicineDTO, Long> {
    Optional<MedicineDTO> findByRegistryNumber(String registryNumber);

    List<MedicineDTO> findByProductNameContainingIgnoreCase(String name);

    List<MedicineDTO> findByActiveIngredientContainingIgnoreCase(String activeIngredient);

    @Query("SELECT m FROM MedicineDTO m WHERE m.cacheExpiry < ?1")
    List<MedicineDTO> findExpiredCache(LocalDateTime now);
}