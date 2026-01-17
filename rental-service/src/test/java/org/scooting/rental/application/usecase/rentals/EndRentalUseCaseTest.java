package org.scooting.rental.application.usecase.rentals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.adapters.message.feign.resilient.ResilientFileClient;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.adapters.message.kafka.UserPublisher;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.usecase.rentals.EndRentalUseCase;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndRentalUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalStatusRepository rentalStatusRepository;

    @Mock
    private TransportClient transportClient;

    @Mock
    private TransportPublisher transportPublisher;

    @Mock
    private UserPublisher userPublisher;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private ResilientFileClient fileClient;

    @InjectMocks
    private EndRentalUseCase endRentalUseCase;

    private Rental activeRental;
    private RentalStatus completedStatus;
    private TransportResponseDTO transportDTO;
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

        rentalResponseDTO = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .transportId(1L)
                .transportType("SCOOTER")
                .status("Завершена")
                .build();
    }

    @Test
    void endRental_Success() {
        // Mock FilePart
        FilePart mockFilePart = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        when(mockFilePart.headers()).thenReturn(headers);

        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("fake-image-data".getBytes());
        when(mockFilePart.content()).thenReturn(Flux.just(dataBuffer));

        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.of(activeRental));
        when(rentalStatusRepository.findByName("COMPLETED"))
                .thenReturn(Optional.of(completedStatus));
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(activeRental);
        when(transportClient.getTransport(1L))
                .thenReturn(transportDTO);
        when(rentalMapper.toResponseDTO(any(Rental.class)))
                .thenReturn(rentalResponseDTO);
        doNothing().when(fileClient).uploadTransportPhoto(any(byte[].class), anyLong());
        doNothing().when(transportPublisher).updateStatus(anyLong(), anyString());
        doNothing().when(userPublisher).awardBonuses(anyLong(), anyInt());

        // Act & Assert
        StepVerifier.create(endRentalUseCase.endRental(100L, 60.5, 30.5, mockFilePart))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();

        verify(fileClient).uploadTransportPhoto(any(byte[].class), eq(100L));
        verify(transportPublisher).updateStatus(1L, "AVAILABLE");
        verify(userPublisher).awardBonuses(eq(100L), anyInt());
    }

    @Test
    void endRental_NoActiveRental_ThrowsException() {
        // Mock FilePart
        FilePart mockFilePart = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        when(mockFilePart.headers()).thenReturn(headers);

        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("fake-image-data".getBytes());
        when(mockFilePart.content()).thenReturn(Flux.just(dataBuffer));

        // Arrange
        when(rentalRepository.findActiveRentalByUserId(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(endRentalUseCase.endRental(100L, 60.5, 30.5, mockFilePart))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("No active rental found")
                )
                .verify();
    }

    @Test
    void endRental_NullPhoto_ThrowsException() {
        // Act & Assert (БЕЗ МОКОВ - они не нужны)
        StepVerifier.create(endRentalUseCase.endRental(100L, 60.5, 30.5, null))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Photo is required")
                )
                .verify();
    }
}