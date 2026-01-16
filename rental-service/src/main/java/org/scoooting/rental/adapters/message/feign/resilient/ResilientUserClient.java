package org.scoooting.rental.adapters.message.feign.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.message.feign.FeignUserClient;
import org.scoooting.rental.application.dto.UserResponseDTO;
import org.scoooting.rental.application.ports.UserClient;
import org.scoooting.rental.domain.exceptions.UserNotFoundException;
import org.scoooting.rental.domain.exceptions.UserServiceException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientUserClient implements UserClient {

    private final FeignUserClient userServiceApi;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponseDTO getUserById(Long id) {
        log.debug("Calling user-service for userId: {}", id);
        try {
            return userServiceApi.getUserById(id).getBody();
        } catch (FeignException.NotFound e) {
            log.error("User {} not found in user-service", id);
            throw new UserNotFoundException("User with ID " + id + " not found");
        } catch (FeignException e) {
            log.error("User service unavailable: {}", e.getMessage());
            throw new UserServiceException("User service is currently unavailable");
        }
    }

    public UserResponseDTO getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUserById! userId: {}, error: {}", id, t.getClass().getSimpleName());
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new UserServiceException("User service unavailable", t);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getIdByCityFallback")
    public Long getIdByCity(String name) {
        log.debug("Calling user-service for city: {}", name);
        try {
            return userServiceApi.getIdByCity(name).getBody();
        } catch (FeignException.NotFound e) {
            throw new UserServiceException("City '" + name + "' not found");
        }
    }

    public Long getIdByCityFallback(String name, Throwable t) {
        log.error("FALLBACK getIdByCity! city: {}, error: {}", name, t.getClass().getSimpleName());
        if (t instanceof UserServiceException && t.getMessage().contains("not found")) {
            throw (UserServiceException) t;
        }
        throw new UserServiceException("User service unavailable", t);
    }
}