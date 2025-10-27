package org.scoooting.rental.clients.resilient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.clients.feign.FeignUserClient;
import org.scoooting.rental.dto.request.UpdateUserRequestDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientUserClient {

    private final FeignUserClient userServiceApi;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public ResponseEntity<UserResponseDTO> getUserById(Long id) {
        log.debug("Calling user-service for userId: {}", id);
        return userServiceApi.getUserById(id);
    }

    public ResponseEntity<UserResponseDTO> getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUserById! userId: {}, error: {}", id, t.getClass().getSimpleName());
        throw new RuntimeException("User service unavailable", t);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    public ResponseEntity<UserResponseDTO> updateUser(Long id, @Valid UpdateUserRequestDTO request) {
        log.debug("Calling user-service to update userId: {}", id);
        return userServiceApi.updateUser(id, request);
    }

    public ResponseEntity<UserResponseDTO> updateUserFallback(Long id, UpdateUserRequestDTO request, Throwable t) {
        log.error("FALLBACK updateUser! userId: {}, error: {}", id, t.getClass().getSimpleName());
        throw new RuntimeException("User service unavailable", t);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getIdByCityFallback")
    public ResponseEntity<Long> getIdByCity(String name) {
        log.debug("Calling user-service for city: {}", name);
        return userServiceApi.getIdByCity(name);
    }

    public ResponseEntity<Long> getIdByCityFallback(String name, Throwable t) {
        log.error("FALLBACK getIdByCity! city: {}, error: {}", name, t.getClass().getSimpleName());
        throw new RuntimeException("User service unavailable", t);
    }
}