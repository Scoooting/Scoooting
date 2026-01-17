package org.scooting.transport.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.transport.application.usecase.TransportStatsUseCase;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.repositories.TransportRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportStatsUseCaseTest {

    @Mock
    private TransportRepository transportRepository;

    @InjectMocks
    private TransportStatsUseCase transportStatsUseCase;

    @Test
    void getAvailabilityStats_Success() {
        // Arrange
        when(transportRepository.countAvailableByType(TransportType.ELECTRIC_KICK_SCOOTER))
                .thenReturn(Mono.just(10L));
        when(transportRepository.countAvailableByType(TransportType.ELECTRIC_BICYCLE))
                .thenReturn(Mono.just(5L));
        when(transportRepository.countAvailableByType(TransportType.ELECTRIC_SCOOTER))
                .thenReturn(Mono.just(3L));
        when(transportRepository.countAvailableByType(TransportType.GAS_MOTORCYCLE))
                .thenReturn(Mono.just(2L));

        // Act & Assert
        StepVerifier.create(transportStatsUseCase.getAvailabilityStats())
                .expectNextMatches(stats ->
                        stats.size() == 4 &&
                                stats.containsKey("ELECTRIC_KICK_SCOOTER") &&
                                stats.containsKey("ELECTRIC_BICYCLE") &&
                                stats.containsKey("ELECTRIC_SCOOTER") &&
                                stats.containsKey("GAS_MOTORCYCLE") &&
                                stats.get("ELECTRIC_KICK_SCOOTER") == 10L &&
                                stats.get("ELECTRIC_BICYCLE") == 5L
                )
                .verifyComplete();
    }

    @Test
    void getAvailabilityStats_EmptyStats() {
        // Arrange
        for (TransportType type : TransportType.values()) {
            when(transportRepository.countAvailableByType(type))
                    .thenReturn(Mono.just(0L));
        }

        // Act & Assert
        StepVerifier.create(transportStatsUseCase.getAvailabilityStats())
                .expectNextMatches(stats ->
                        stats.size() == 4 &&
                                stats.values().stream().allMatch(count -> count == 0L)
                )
                .verifyComplete();
    }
}