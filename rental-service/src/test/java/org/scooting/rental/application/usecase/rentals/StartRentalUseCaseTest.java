package org.scooting.rental.application.usecase.rentals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.timeout;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.usecase.rentals.StartRentalUseCase;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartRentalUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalStatusRepository rentalStatusRepository;

    @Mock
    private TransportClient transportClient;

    @Mock
    private TransportPublisher transportPublisher;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private StartRentalUseCase startRentalUseCase;

    private RentalStatus activeStatus;
    private TransportResponseDTO transportDTO;
    private Rental savedRental;
    private RentalResponseDTO rentalResponseDTO;

    @BeforeEach
    void setUp() {
        activeStatus = new RentalStatus(1L, "ACTIVE");

        transportDTO = new TransportResponseDTO(
                1L, "SCOOTER", "AVAILABLE", 60.0, 30.0, "SPB"
        );

        savedRental = new Rental();
        savedRental.setId(1L);
        savedRental.setUserId(100L);
        savedRental.setTransportId(1L);
        savedRental.setStatusId(1L);
        savedRental.setStartLatitude(60.0);
        savedRental.setStartLongitude(30.0);
        savedRental.setStartTime(Instant.now());

        rentalResponseDTO = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .transportId(1L)
                .transportType("SCOOTER")
                .status("Активна")
                .build();
    }

    @Test
    void startRental_Success() {
        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.empty());
        when(rentalStatusRepository.findByName("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));
        lenient().when(transportClient.getTransport(1L))
                .thenReturn(transportDTO);
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(savedRental);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);

        // Act & Assert
        StepVerifier.create(startRentalUseCase.startRental(100L, 1L, 60.0, 30.0))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getUserId().equals(100L) &&
                                dto.getTransportId().equals(1L)
                )
                .verifyComplete();

        verify(rentalRepository, timeout(1000)).save(any(Rental.class));
        verify(transportPublisher, timeout(1000)).updateStatus(1L, "IN_USE");  // <-- ИСПРАВЛЕНО
    }

    @Test
    void startRental_UserAlreadyHasActiveRental_ThrowsException() {
        // Arrange
        Rental existingRental = new Rental();
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.of(existingRental));

        // Act & Assert
        StepVerifier.create(startRentalUseCase.startRental(100L, 1L, 60.0, 30.0))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("already has an active rental")
                )
                .verify();

        verify(rentalRepository, never()).save(any());
        verify(transportPublisher, never()).updateStatus(anyLong(), anyString());
    }
}