package org.scooting.rental.application.usecase.rentals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.usecase.rentals.RentalHistoryUseCase;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.repositories.RentalRepository;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalHistoryUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalHistoryUseCase rentalHistoryUseCase;

    private Rental completedRental;
    private RentalResponseDTO rentalResponseDTO;

    @BeforeEach
    void setUp() {
        completedRental = new Rental();
        completedRental.setId(1L);
        completedRental.setUserId(100L);
        completedRental.setTransportId(1L);
        completedRental.setStartTime(Instant.now().minusSeconds(3600));
        completedRental.setEndTime(Instant.now());
        completedRental.setDurationMinutes(60);
        completedRental.setTotalCost(new BigDecimal("31.00"));
        completedRental.setStatusId(2L);

        rentalResponseDTO = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .transportId(1L)
                .durationMinutes(60)
                .totalCost(new BigDecimal("31.00"))
                .build();
    }

    @Test
    void getUserRentalHistory_Success() {
        // Arrange
        List<Rental> rentals = List.of(completedRental);
        when(rentalRepository.findRentalHistoryByUserId(100L, 0, 20))
                .thenReturn(rentals);
        when(rentalRepository.countRentalsByUserId(100L))
                .thenReturn(1L);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);

        // Act & Assert
        StepVerifier.create(rentalHistoryUseCase.getUserRentalHistory(100L, 0, 20))
                .expectNextMatches(page ->
                        page.content().size() == 1 &&
                                page.totalElements() == 1 &&
                                page.first() &&          // <-- исправлено
                                page.last()              // <-- исправлено
                )
                .verifyComplete();

        verify(rentalRepository).findRentalHistoryByUserId(100L, 0, 20);
        verify(rentalRepository).countRentalsByUserId(100L);
    }

    @Test
    void getUserRentalHistory_EmptyHistory() {
        // Arrange
        when(rentalRepository.findRentalHistoryByUserId(100L, 0, 20))
                .thenReturn(List.of());
        when(rentalRepository.countRentalsByUserId(100L))
                .thenReturn(0L);

        // Act & Assert
        StepVerifier.create(rentalHistoryUseCase.getUserRentalHistory(100L, 0, 20))
                .expectNextMatches(page ->
                        page.content().isEmpty() &&
                                page.totalElements() == 0
                )
                .verifyComplete();
    }

    @Test
    void getUserRentalHistory_Pagination() {
        // Arrange
        when(rentalRepository.findRentalHistoryByUserId(100L, 20, 20))
                .thenReturn(List.of(completedRental));
        when(rentalRepository.countRentalsByUserId(100L))
                .thenReturn(50L);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);

        // Act & Assert
        StepVerifier.create(rentalHistoryUseCase.getUserRentalHistory(100L, 1, 20))
                .expectNextMatches(page ->
                        page.page() == 1 &&           // <-- исправлено
                                page.totalPages() == 3 &&
                                !page.first() &&
                                !page.last()
                )
                .verifyComplete();
    }
}