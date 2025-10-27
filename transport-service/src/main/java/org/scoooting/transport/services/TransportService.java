package org.scoooting.transport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.clients.resilient.ResilientUserClient;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportStatusRepository statusRepository;
    private final TransportMapper transportMapper;
    private final ResilientUserClient resilientUserClient;

    @Transactional(readOnly = true)
    public Flux<TransportResponseDTO> findNearestTransports(Double lat, Double lng, Double radiusKm) {
        // Calculate boundaries
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return transportRepository.findAvailableInArea(
                lat - latRange, lat + latRange,
                lng - lngRange, lng + lngRange
        ).flatMap(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Flux<TransportResponseDTO> findTransportsByType(
            TransportType type, Double lat, Double lng, Double radiusKm
    ) {
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return transportRepository.findAvailableByTypeInArea(
                type, lat - latRange, lat + latRange, lng - lngRange, lng + lngRange
        ).flatMap(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Mono<TransportResponseDTO> getTransportById(Long id) {
        return transportRepository.findById(id)
                .switchIfEmpty(Mono.error(new TransportNotFoundException("Transport not found")))
                .flatMap(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Flux<TransportResponseDTO> findAvailableTransportsByType(TransportType type) {
        return transportRepository.findAvailableByType(type)
                .flatMap(this::toResponseDTO);
    }

    public Mono<Map<String, Long>> getAvailabilityStats() {
        return Flux.fromArray(TransportType.values())
                .flatMap(type -> transportRepository.countAvailableByType(type)
                        .map(count -> Map.entry(type.name(), count)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

        public Mono<TransportResponseDTO> updateTransportStatus(Long transportId, String statusName) {

            return transportRepository.findById(transportId)
                .switchIfEmpty(Mono.error(new TransportNotFoundException("Transport not found")))
                .flatMap(transport -> statusRepository.findByName(statusName)
                .switchIfEmpty(Mono.error(new DataNotFoundException("Status not found")))
                .flatMap(status -> {
                    transport.setStatusId(status.getId());
                    return transportRepository.save(transport);
                }))
                .flatMap(this::toResponseDTO);
        }

    public Mono<Long> getStatusId(String name) {
        return statusRepository.findByName(name)
            .map(TransportStatus::getId)
            .switchIfEmpty(Mono.error(new DataNotFoundException("Status not found")));
    }

    public Mono<Void> updateCoordinates(UpdateCoordinatesDTO dto) {
        return transportRepository.findById(dto.transportId())
            .switchIfEmpty(Mono.error(new TransportNotFoundException("Transport not found")))
            .flatMap(transport -> {
                transport.setLatitude(dto.latitude());
                transport.setLongitude(dto.longitude());
                return transportRepository.save(transport);
            })
            .then();
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