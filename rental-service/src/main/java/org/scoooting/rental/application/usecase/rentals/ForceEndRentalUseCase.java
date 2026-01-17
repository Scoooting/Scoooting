package org.scoooting.rental.application.usecase.rentals;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.adapters.message.kafka.UserPublisher;
import org.scoooting.rental.adapters.security.UserPrincipal;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.ports.UserClient;
import org.scoooting.rental.application.services.Distance;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.scoooting.rental.adapters.message.kafka.dto.ForceEndRentalDto;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.dto.UserResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
public class ForceEndRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final TransportClient transportClient;
    private final UserClient userClient;  // НУЖЕН для getUserById!
    private final TransportPublisher transportPublisher;  // Добавил
    private final UserPublisher userPublisher;  // Добавил
    private final RentalMapper rentalMapper;

    private static final BigDecimal BASE_RATE = new BigDecimal("0.50");
    private static final BigDecimal UNLOCK_FEE = new BigDecimal("1.00");

    public Mono<ForceEndRentalDto> forceEndRental(Long rentalId, Double endLat, Double endLng) {
        return Mono.fromCallable(() -> forceEndRentalBlocking(rentalId, endLat, endLng))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    protected ForceEndRentalDto forceEndRentalBlocking(Long rentalId, Double endLat, Double endLng) {
        // Find rental by ID
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new DataNotFoundException("Rental not found"));

        // Check if already ended
        if (rental.getEndTime() != null) {
            throw new IllegalStateException("Rental already ended");
        }

        // Calculate duration and cost
        Instant endTime = Instant.now();
        long minutes = Duration.between(rental.getStartTime(), endTime).toMinutes();
        BigDecimal totalCost = UNLOCK_FEE.add(BASE_RATE.multiply(BigDecimal.valueOf(minutes)));

        // Calculate distance
        double distance = Distance.calculateDistance(
                rental.getStartLatitude(), rental.getStartLongitude(),
                endLat, endLng
        );

        // Update rental
        rental.setEndTime(endTime);
        rental.setEndLatitude(endLat);
        rental.setEndLongitude(endLng);
        rental.setDurationMinutes((int) minutes);
        rental.setTotalCost(totalCost);
        rental.setDistanceKm(BigDecimal.valueOf(distance));

        // Set COMPLETED status
        RentalStatus completedStatus = rentalStatusRepository.findByName("COMPLETED")
                .orElseThrow(() -> new DataNotFoundException("COMPLETED status not found"));
        rental.setStatusId(completedStatus.getId());

        rental = rentalRepository.save(rental);

        // TRANSACTION ENDS HERE

        // Get transport info
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId());

        // Get user info for response
        UserResponseDTO user = userClient.getUserById(rental.getUserId());

        // Publish commands to Kafka (async)
        transportPublisher.updateStatus(transport.id(), "AVAILABLE");
        transportPublisher.updateCoordinates(transport.id(), endLat, endLng);
        userPublisher.awardBonuses(rental.getUserId(), (int) minutes);

        // Build response
        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Принудительно завершена");

        return new ForceEndRentalDto(rentalResponseDTO,
                new UserPrincipal(user.name(), user.id(), user.email(), user.role()));
    }
}