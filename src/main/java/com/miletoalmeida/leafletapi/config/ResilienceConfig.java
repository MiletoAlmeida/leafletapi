package com.miletoalmeida.leafletapi.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)                // Porcentagem de falhas para abrir o circuito
                .waitDurationInOpenState(Duration.ofSeconds(20))  // Tempo em estado aberto
                .slidingWindowSize(5)                    // Número de chamadas para considerar
                .minimumNumberOfCalls(3)                // Mínimo de chamadas antes de calcular taxa de falha
                .permittedNumberOfCallsInHalfOpenState(2) // Chamadas permitidas em estado semi-aberto
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public CircuitBreaker anvisaServiceCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("anvisaService");
    }
}