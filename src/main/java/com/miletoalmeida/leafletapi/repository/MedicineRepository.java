package com.miletoalmeida.leafletapi.repository;

import com.miletoalmeida.leafletapi.model.DTO.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByRegistryNumber(String registryNumber);

    List<Medicine> findByProductNameContainingIgnoreCase(String name);

    List<Medicine> findByActiveIngredientContainingIgnoreCase(String activeIngredient);

    @Query("SELECT m FROM Medicine m WHERE m.cacheExpiry < ?1")
    List<Medicine> findExpiredCache(LocalDateTime now);
}