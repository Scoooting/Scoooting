package org.scoooting.transport.adapters.infrastructure.messaging.feign.resilient;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.application.ports.UserClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientUserClient implements UserClient {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://user-service/api").build();
    }

    @Override
    public Mono<String> getCityName(Long cityId) {
        if (cityId == null) {
            return Mono.just("N/A");
        }

        log.debug("Calling user-service for cityId: {}", cityId);

        return getWebClient()
                .get()
                .uri("/cities/city/{id}", cityId)
                .retrieve()
                .bodyToMono(String.class)
                .transformDeferred(CircuitBreakerOperator.of(
                        circuitBreakerRegistry.circuitBreaker("userService")
                ))
                .doOnError(e -> log.error("Error calling user-service: {}", e.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    log.warn("City {} not found", cityId);
                    return Mono.just("Unknown");
                })
                .onErrorResume(e -> {
                    log.error("FALLBACK getCityName! cityId: {}, error: {}",
                            cityId, e.getClass().getSimpleName());
                    return Mono.just("N/A");
                });
    }
}