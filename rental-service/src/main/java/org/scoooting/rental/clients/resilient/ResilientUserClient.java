package org.scoooting.rental.clients.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    // Для валидации СЕБЯ (USER может вызвать /me)
    @CircuitBreaker(name = "userService", fallbackMethod = "getCurrentUserFallback")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        log.debug("Calling user-service /me");
        try {
            return userServiceApi.getCurrentUser();
        } catch (FeignException.NotFound e) {
            log.error("Current user not found in user-service");
            throw new UserNotFoundException("Current user not found");
        } catch (FeignException e) {
            log.error("User service unavailable: {}", e.getMessage());
            throw new UserServiceException("User service is currently unavailable");
        }
    }

    public ResponseEntity<UserResponseDTO> getCurrentUserFallback(Throwable t) {
        log.error("FALLBACK getCurrentUser! error: {}", t.getClass().getSimpleName());
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new UserServiceException("User service unavailable", t);
    }

    // Для валидации ДРУГОГО пользователя (только SUPPORT/ANALYST/ADMIN)
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
        }
    }

    public ResponseEntity<UserResponseDTO> getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUserById! userId: {}, error: {}", id, t.getClass().getSimpleName());
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new UserServiceException("User service unavailable", t);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "addBonusesFallback")
    public ResponseEntity<UserResponseDTO> addBonuses(Long id, Integer bonuses) {
        log.debug("Calling addBonuses: {}", id);
        try {
            return userServiceApi.addBonuses(id, bonuses);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        } catch (FeignException e) {
            throw new UserServiceException("User service is currently unavailable");
        }
    }

    public ResponseEntity<UserResponseDTO> addBonusesFallback(Long id, Integer bonuses, Throwable t) {
        log.error("FALLBACK addBonuses! userId: {}, error: {}", id, t.getClass().getSimpleName());
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new UserServiceException("User service unavailable", t);
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
        if (t instanceof UserServiceException && t.getMessage().contains("not found")) {
            throw (UserServiceException) t;
        }
        throw new UserServiceException("User service unavailable", t);
    }
}