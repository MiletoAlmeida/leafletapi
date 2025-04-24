package com.miletoalmeida.leafletapi.repository;

import com.miletoalmeida.leafletapi.model.CacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntry, Long> {
    Optional<CacheEntry> findByCacheKeyAndType(String cacheKey, String type);

    @Query("SELECT c FROM CacheEntry c WHERE c.expiresAt < ?1")
    List<CacheEntry> findExpiredEntries(LocalDateTime now);
}