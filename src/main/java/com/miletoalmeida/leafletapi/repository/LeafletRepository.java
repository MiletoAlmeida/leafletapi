package com.miletoalmeida.leafletapi.repository;

import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeafletRepository extends JpaRepository<LeafletDTO, Long> {
    Optional<LeafletDTO> findByMedicineRegistryNumber(String registryNumber);

    @Query("SELECT l FROM LeafletDTO l WHERE l.cacheExpiry < ?1")
    List<LeafletDTO> findExpiredCache(LocalDateTime now);
}