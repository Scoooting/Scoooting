package org.scooting.rental.application.usecase.rentals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.usecase.rentals.CancelRentalUseCase;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelRentalUseCaseTest {

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
    private CancelRentalUseCase cancelRentalUseCase;

    private Rental activeRental;
    private RentalStatus cancelledStatus;
    private TransportResponseDTO transportDTO;
    private RentalResponseDTO rentalResponseDTO;

    @BeforeEach
    void setUp() {
        activeRental = new Rental();
        activeRental.setId(1L);
        activeRental.setUserId(100L);
        activeRental.setTransportId(1L);
        activeRental.setStartTime(Instant.now());
        activeRental.setStatusId(1L);

        cancelledStatus = new RentalStatus(3L, "CANCELLED");

        transportDTO = new TransportResponseDTO(
                1L, "SCOOTER", "BUSY", 60.0, 30.0, "SPB"
        );

        rentalResponseDTO = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .transportId(1L)
                .transportType("SCOOTER")
                .status("Отменена")
                .durationMinutes(0)
                .totalCost(BigDecimal.ZERO)
                .build();
    }

    @Test
    void cancelRental_Success() {
        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.of(activeRental));
        when(rentalStatusRepository.findByName("CANCELLED"))
                .thenReturn(Optional.of(cancelledStatus));
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(activeRental);
        when(transportClient.getTransport(1L))
                .thenReturn(transportDTO);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);
        doNothing().when(transportPublisher).updateStatus(anyLong(), anyString());

        // Act & Assert
        StepVerifier.create(cancelRentalUseCase.cancelRental(100L))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getDurationMinutes().equals(0) &&
                                dto.getTotalCost().equals(BigDecimal.ZERO)
                )
                .verifyComplete();

        verify(rentalRepository).save(any(Rental.class));
        verify(transportPublisher).updateStatus(1L, "AVAILABLE");
    }

    @Test
    void cancelRental_NoActiveRental_ThrowsException() {
        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(cancelRentalUseCase.cancelRental(100L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("No active rental found")
                )
                .verify();

        verify(rentalRepository, never()).save(any());
        verify(transportPublisher, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void cancelRental_AlreadyEnded_ThrowsException() {
        // Arrange
        activeRental.setEndTime(Instant.now());
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.of(activeRental));

        // Act & Assert
        StepVerifier.create(cancelRentalUseCase.cancelRental(100L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("already ended or cancelled")
                )
                .verify();
    }
}