package org.scoooting.rental.application.usecase.rentals;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@RequiredArgsConstructor
public class StartRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final TransportClient transportClient;
    private final TransportPublisher transportPublisher;
    private final RentalMapper rentalMapper;

    public Mono<RentalResponseDTO> startRental(Long userId, Long transportId, Double startLat, Double startLng) {
        return Mono.fromCallable(() -> startRentalBlocking(userId, transportId, startLat, startLng))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    protected RentalResponseDTO startRentalBlocking(Long userId, Long transportId, Double startLat, Double startLng) {
        if (rentalRepository.findActiveRentalByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has an active rental");
        }

        RentalStatus rentalStatus = rentalStatusRepository.findByName("ACTIVE")
                .orElseThrow(() -> new DataNotFoundException("ACTIVE status not found"));

        Rental rental = Rental.builder()
                .userId(userId)
                .transportId(transportId)
                .statusId(rentalStatus.getId())
                .startTime(Instant.now())
                .startLatitude(startLat)
                .startLongitude(startLng)
                .build();
        rental = rentalRepository.save(rental);

        // Publish to Kafka instead of HTTP
        transportPublisher.updateStatus(transportId, "IN_USE");

        return rentalMapper.toResponseDTO(rental);
    }
}