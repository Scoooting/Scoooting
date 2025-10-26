package org.scoooting.transport.clients.resilient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.clients.feign.FeignUserClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientUserClient {

    private final FeignUserClient feignUserClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getCityNameFallback")
    public String getCityName(Long cityId) {
        if (cityId == null) {
            return null;
        }

        log.info("Calling user-service for cityId: {}", cityId);
        return feignUserClient.getCityById(cityId).getBody();
    }

    public String getCityNameFallback(Long cityId, Throwable throwable) {
        log.error("FALLBACK ACTIVATED! cityId: {}, error: {}",
                cityId, throwable.getClass().getSimpleName());
        return "N/A";
    }
}