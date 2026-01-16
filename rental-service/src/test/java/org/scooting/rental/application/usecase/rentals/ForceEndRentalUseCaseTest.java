package org.scooting.rental.application.usecase.rentals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.adapters.message.kafka.UserPublisher;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.dto.UserResponseDTO;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.ports.UserClient;
import org.scoooting.rental.application.usecase.rentals.ForceEndRentalUseCase;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
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
class ForceEndRentalUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalStatusRepository rentalStatusRepository;

    @Mock
    private TransportClient transportClient;

    @Mock
    private UserClient userClient;

    @Mock
    private TransportPublisher transportPublisher;

    @Mock
    private UserPublisher userPublisher;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private ForceEndRentalUseCase forceEndRentalUseCase;

    private Rental activeRental;
    private RentalStatus completedStatus;
    private TransportResponseDTO transportDTO;
    private UserResponseDTO userDTO;
    private RentalResponseDTO rentalResponseDTO;

    @BeforeEach
    void setUp() {
        activeRental = new Rental();
        activeRental.setId(1L);
        activeRental.setUserId(100L);
        activeRental.setTransportId(1L);
        activeRental.setStartLatitude(60.0);
        activeRental.setStartLongitude(30.0);
        activeRental.setStartTime(Instant.now().minusSeconds(600));
        activeRental.setStatusId(1L);

        completedStatus = new RentalStatus(2L, "COMPLETED");

        transportDTO = new TransportResponseDTO(
                1L, "SCOOTER", "IN_USE", 60.0, 30.0, "SPB"
        );

        userDTO = new UserResponseDTO(
                100L, "testuser", "test@example.com", "USER", "SPB", 1
        );

        rentalResponseDTO = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .transportId(1L)
                .status("Принудительно завершена")
                .build();
    }

    @Test
    void forceEndRental_Success() {
        // Arrange
        when(rentalRepository.findById(1L))
                .thenReturn(Optional.of(activeRental));
        when(rentalStatusRepository.findByName("COMPLETED"))
                .thenReturn(Optional.of(completedStatus));
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(activeRental);
        when(transportClient.getTransport(1L))
                .thenReturn(transportDTO);
        when(userClient.getUserById(100L))
                .thenReturn(userDTO);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);
        doNothing().when(transportPublisher).updateStatus(anyLong(), anyString());
        doNothing().when(userPublisher).awardBonuses(anyLong(), anyInt());

        // Act & Assert
        StepVerifier.create(forceEndRentalUseCase.forceEndRental(1L, 60.5, 30.5))
                .expectNextMatches(dto ->
                        dto.rentalResponseDTO().getId().equals(1L) &&
                                dto.userPrincipal().getUserId().equals(100L)
                )
                .verifyComplete();

        verify(rentalRepository).save(any(Rental.class));
        verify(transportPublisher).updateStatus(1L, "AVAILABLE");
        verify(userPublisher).awardBonuses(eq(100L), anyInt());
    }

    @Test
    void forceEndRental_RentalNotFound_ThrowsException() {
        // Arrange
        when(rentalRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(forceEndRentalUseCase.forceEndRental(1L, 60.5, 30.5))
                .expectError(DataNotFoundException.class)
                .verify();

        verify(rentalRepository, never()).save(any());
    }

    @Test
    void forceEndRental_AlreadyEnded_ThrowsException() {
        // Arrange
        activeRental.setEndTime(Instant.now());
        when(rentalRepository.findById(1L))
                .thenReturn(Optional.of(activeRental));

        // Act & Assert
        StepVerifier.create(forceEndRentalUseCase.forceEndRental(1L, 60.5, 30.5))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("already ended")
                )
                .verify();
    }
}