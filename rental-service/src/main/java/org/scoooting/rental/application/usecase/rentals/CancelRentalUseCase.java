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
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
public class CancelRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final TransportClient transportClient;
    private final TransportPublisher transportPublisher;
    private final RentalMapper rentalMapper;

    public Mono<RentalResponseDTO> cancelRental(Long userId) {
        return Mono.fromCallable(() -> cancelRentalBlocking(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    protected RentalResponseDTO cancelRentalBlocking(Long userId) {
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found"));

        if (rental.getEndTime() != null) {
            throw new IllegalStateException("Rental already ended or cancelled");
        }

        RentalStatus cancelledStatus = rentalStatusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new DataNotFoundException("CANCELLED status not found"));
        rental.setStatusId(cancelledStatus.getId());
        rental.setEndTime(Instant.now());
        rental.setDurationMinutes(0);
        rental.setTotalCost(BigDecimal.valueOf(0));
        rentalRepository.save(rental);

        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId());

        // Publish to Kafka instead of HTTP
        transportPublisher.updateStatus(transport.id(), "AVAILABLE");

        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Отменена");

        return rentalResponseDTO;
    }
}