package com.miletoalmeida.leafletapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miletoalmeida.leafletapi.model.CacheEntry;
import com.miletoalmeida.leafletapi.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CacheService {

    private final CacheRepository cacheRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CacheService(CacheRepository cacheRepository, ObjectMapper objectMapper) {
        this.cacheRepository = cacheRepository;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> getFromCache(String key, String type, Class<T> valueType) {
        Optional<CacheEntry> cacheEntry = cacheRepository.findByCacheKeyAndType(key, type);

        if (cacheEntry.isPresent()) {
            CacheEntry entry = cacheEntry.get();

            // Check if cache is still valid
            if (entry.getExpiresAt().isAfter(LocalDateTime.now())) {
                try {
                    return Optional.of(objectMapper.readValue(entry.getCacheValue(), valueType));
                } catch (JsonProcessingException e) {
                    // If deserialization fails, return empty
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    public <T> void saveToCache(String key, String type, T value, long expiryInMinutes) {
        try {
            String serializedValue = objectMapper.writeValueAsString(value);

            Optional<CacheEntry> existingEntry = cacheRepository.findByCacheKeyAndType(key, type);
            CacheEntry entry;

            if (existingEntry.isPresent()) {
                entry = existingEntry.get();
                entry.setCacheValue(serializedValue);
                entry.setExpiresAt(LocalDateTime.now().plusMinutes(expiryInMinutes));
            } else {
                entry = new CacheEntry();
                entry.setCacheKey(key);
                entry.setType(type);
                entry.setCacheValue(serializedValue);
                entry.setCreatedAt(LocalDateTime.now());
                entry.setExpiresAt(LocalDateTime.now().plusMinutes(expiryInMinutes));
            }

            cacheRepository.save(entry);

        } catch (JsonProcessingException e) {
            // Log error but continue
            System.err.println("Failed to serialize cache value: " + e.getMessage());
        }
    }

    public void invalidateCache(String key, String type) {
        Optional<CacheEntry> entry = cacheRepository.findByCacheKeyAndType(key, type);
        entry.ifPresent(cacheRepository::delete);
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Run once a day
    public void cleanupExpiredCache() {
        LocalDateTime now = LocalDateTime.now();
        cacheRepository.findExpiredEntries(now).forEach(cacheRepository::delete);
    }
}