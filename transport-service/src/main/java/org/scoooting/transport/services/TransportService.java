package org.scoooting.transport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.clients.resilient.ResilientUserClient;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.ScrollResponseDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.Transport;
import org.scoooting.transport.entities.TransportStatus;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.exceptions.DataNotFoundException;
import org.scoooting.transport.exceptions.TransportNotFoundException;
import org.scoooting.transport.mappers.TransportMapper;
import org.scoooting.transport.repositories.TransportRepository;
import org.scoooting.transport.repositories.TransportStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportStatusRepository statusRepository;
    private final TransportMapper transportMapper;
    private final ResilientUserClient resilientUserClient;
    private final TransactionalOperator transactionalOperator;

    /**
     * Find nearest transport in requested radius
     *
     * Transactional operator IS NEEDED due to N+1 problem:
     * - without transaction: 1 + N requests to db == N+1 connections
     * - with transaction: N requests in the same connection
     *
     * @param lat
     * @param lng
     * @param radiusKm
     * @return nearest transport from requested latitude and longitude in requested radius
     */
    public Flux<TransportResponseDTO> findNearestTransports(Double lat, Double lng, Double radiusKm) {
        // Calculate boundaries
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return transportRepository.findAvailableInArea(
                lat - latRange, lat + latRange,
                lng - lngRange, lng + lngRange
        )
                .flatMap(this::toResponseDTO)
                .as(transactionalOperator::transactional);
    }

    public Flux<TransportResponseDTO> findTransportsByType(
            TransportType type, Double lat, Double lng, Double radiusKm
    ) {
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return transportRepository.findAvailableByTypeInArea(
                type, lat - latRange, lat + latRange, lng - lngRange, lng + lngRange
        ).flatMap(this::toResponseDTO);
    }

    public Mono<TransportResponseDTO> getTransportById(Long id) {
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("Transport ID must be positive"));
        }

        return transportRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new TransportNotFoundException("Transport with id " + id + " not found")
                ))
                .flatMap(this::toResponseDTO)
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.error(new IllegalArgumentException("Invalid transport ID: " + id))
                );
    }

    public Mono<ScrollResponseDTO<TransportResponseDTO>> scrollAvailableTransportsByType(
            TransportType type, int page, int size
    ) {
        return transportRepository.findAvailableByType(type)
                .skip((long) page * size)  // skip prev pages
                .take(size + 1)                 // Get size+1 to prevent hasMore
                .flatMap(this::toResponseDTO)
                .collectList()
                .map(list -> {
                    boolean hasMore = list.size() > size;
                    List<TransportResponseDTO> content = hasMore
                            ? list.subList(0, size)
                            : list;
                    return new ScrollResponseDTO<>(content, page, size, hasMore);
                });
    }

    public Mono<Map<String, Long>> getAvailabilityStats() {
        return Flux.fromArray(TransportType.values())
                .flatMap(type -> transportRepository.countAvailableByType(type)
                        .map(count -> Map.entry(type.name(), count)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    // 2+ atomic
    public Mono<TransportResponseDTO> updateTransportStatus(Long transportId, String statusName) {

        return transportRepository.findById(transportId)
            .switchIfEmpty(Mono.error(new TransportNotFoundException("Transport not found")))
            .flatMap(transport -> statusRepository.findByName(statusName)
            .switchIfEmpty(Mono.error(new DataNotFoundException("Status not found")))
            .flatMap(status -> {
                transport.setStatusId(status.getId());
                return transportRepository.save(transport);
            }))
            .as(transactionalOperator::transactional)
            .flatMap(this::toResponseDTO);
    }

    // find + update
    public Mono<TransportResponseDTO> updateCoordinates(UpdateCoordinatesDTO dto) {
        if (dto.latitude() < -90 || dto.latitude() > 90) {
            return Mono.error(new IllegalArgumentException(
                    "Latitude must be between -90 and 90, got: " + dto.latitude()
            ));
        }
        if (dto.longitude() < -180 || dto.longitude() > 180) {
            return Mono.error(new IllegalArgumentException(
                    "Longitude must be between -180 and 180, got: " + dto.longitude()
            ));
        }

        return transportRepository.findById(dto.transportId())
                .switchIfEmpty(Mono.error(
                        new TransportNotFoundException("Transport with id " + dto.transportId() + " not found")
                ))
                .flatMap(transport -> {
                    transport.setLatitude(dto.latitude());
                    transport.setLongitude(dto.longitude());
                    return transportRepository.save(transport);
                })
                .as(transactionalOperator::transactional)  // ← Atomic: SELECT + UPDATE
                .flatMap(this::toResponseDTO);  // return updated obj
    }

    public Mono<TransportResponseDTO> toResponseDTO(Transport transport) {
        return statusRepository.findById(transport.getStatusId())
                .map(TransportStatus::getName)
                .defaultIfEmpty("UNKNOWN")
                .flatMap(statusName ->
                    resilientUserClient.getCityName(transport.getCityId())
                    .map(cityName -> transportMapper.toResponseDTO(transport, statusName, cityName))
                );
    }
}