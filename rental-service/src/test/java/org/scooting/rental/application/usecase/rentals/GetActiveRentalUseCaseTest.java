package org.scooting.rental.application.usecase.rentals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.application.dto.PageResponseDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.usecase.rentals.GetActiveRentalUseCase;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.repositories.RentalRepository;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetActiveRentalUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private GetActiveRentalUseCase getActiveRentalUseCase;

    private Rental activeRental;
    private RentalResponseDTO rentalResponseDTO;

    @BeforeEach
    void setUp() {
        activeRental = new Rental();
        activeRental.setId(1L);
        activeRental.setUserId(100L);
        activeRental.setTransportId(1L);
        activeRental.setStartTime(Instant.now());
        activeRental.setStatusId(1L);

        rentalResponseDTO = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .transportId(1L)
                .transportType("SCOOTER")
                .status("Активна")
                .build();
    }

    @Test
    void getActiveRental_Success() {
        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.of(activeRental));
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);

        // Act & Assert
        StepVerifier.create(getActiveRentalUseCase.getActiveRental(100L))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();

        verify(rentalRepository).findActiveRentalByUserId(100L);
        verify(rentalMapper).toResponseDTO(activeRental);
    }

    @Test
    void getActiveRental_NotFound_ThrowsException() {
        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(getActiveRentalUseCase.getActiveRental(100L))
                .expectError(DataNotFoundException.class)
                .verify();

        verify(rentalMapper, never()).toResponseDTO(any());
    }

    @Test
    void getAllRentals_Success() {
        // Arrange
        List<Rental> rentals = List.of(activeRental);
        when(rentalRepository.findAllRentals(0, 20))
                .thenReturn(rentals);
        when(rentalRepository.countAllRentals())
                .thenReturn(1L);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);

        // Act & Assert
        StepVerifier.create(getActiveRentalUseCase.getAllRentals(0, 20))
                .expectNextMatches(page ->
                        page.content().size() == 1 &&
                                page.totalElements() == 1 &&
                                page.totalPages() == 1
                )
                .verifyComplete();

        verify(rentalRepository).findAllRentals(0, 20);
        verify(rentalRepository).countAllRentals();
    }

    @Test
    void getAllRentals_EmptyList() {
        // Arrange
        when(rentalRepository.findAllRentals(0, 20))
                .thenReturn(List.of());
        when(rentalRepository.countAllRentals())
                .thenReturn(0L);

        // Act & Assert
        StepVerifier.create(getActiveRentalUseCase.getAllRentals(0, 20))
                .expectNextMatches(page ->
                        page.content().isEmpty() &&
                                page.totalElements() == 0
                )
                .verifyComplete();
    }
}