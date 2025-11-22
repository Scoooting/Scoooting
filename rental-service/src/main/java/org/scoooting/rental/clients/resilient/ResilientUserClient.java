package org.scoooting.rental.clients.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.clients.feign.FeignUserClient;
import org.scoooting.rental.dto.request.UpdateUserRequestDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.scoooting.rental.exceptions.UserNotFoundException;
import org.scoooting.rental.exceptions.UserServiceException;
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
        try {
            return userServiceApi.getUserById(id);
        } catch (FeignException.NotFound e) {
            log.error("User {} not found in user-service", id);
            throw new UserNotFoundException("User with ID " + id + " not found");
        } catch (FeignException e) {
            log.error("User service unavailable: {}", e.getMessage());
            throw new UserServiceException("User service is currently unavailable");
        }    }

    public ResponseEntity<UserResponseDTO> getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUserById! userId: {}, error: {}", id, t.getClass().getSimpleName());

        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }

        if (t instanceof UserServiceException) {
            throw (UserServiceException) t;
        }

        throw new UserServiceException("User service unavailable", t);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    public ResponseEntity<UserResponseDTO> updateUser(Long id, @Valid UpdateUserRequestDTO request) {
        log.debug("Calling user-service to update userId: {}", id);
        try {
            return userServiceApi.updateUser(id, request);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        } catch (FeignException e) {
            throw new UserServiceException("User service is currently unavailable");
        }    }

    public ResponseEntity<UserResponseDTO> updateUserFallback(Long id, UpdateUserRequestDTO request, Throwable t) {
        log.error("FALLBACK updateUser! userId: {}, error: {}", id, t.getClass().getSimpleName());

        if (!(t instanceof UserServiceException && t.getMessage().contains("not found"))) {
            throw new UserServiceException("User service unavailable", t);
        }
        throw (UserServiceException) t;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getIdByCityFallback")
    public ResponseEntity<Long> getIdByCity(String name) {
        log.debug("Calling user-service for city: {}", name);
        try {
            return userServiceApi.getIdByCity(name);
        } catch (FeignException.NotFound e) {
            throw new UserServiceException("City '" + name + "' not found");
        }
    }

    public ResponseEntity<Long> getIdByCityFallback(String name, Throwable t) {
        log.error("FALLBACK getIdByCity! city: {}, error: {}", name, t.getClass().getSimpleName());

        if (!(t instanceof UserServiceException && t.getMessage().contains("not found"))) {
            throw new UserServiceException("User service unavailable", t);
        }
        throw (UserServiceException) t;
    }
}