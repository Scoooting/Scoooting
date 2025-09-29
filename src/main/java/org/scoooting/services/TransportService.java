package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.response.TransportResponseDTO;
import org.scoooting.entities.City;
import org.scoooting.entities.Transport;
import org.scoooting.entities.TransportStatus;
import org.scoooting.entities.enums.*;
import org.scoooting.exceptions.common.DataNotFoundException;
import org.scoooting.exceptions.transport.TransportNotFoundException;
import org.scoooting.mappers.TransportMapper;
import org.scoooting.repositories.CityRepository;
import org.scoooting.repositories.TransportRepository;
import org.scoooting.repositories.TransportStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportStatusRepository statusRepository;
    private final CityRepository cityRepository;
    private final TransportMapper transportMapper;

    @Transactional(readOnly = true)
    public List<TransportResponseDTO> findNearestTransports(Double lat, Double lng, Double radiusKm) {
        // Calculate boundaries
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        List<Transport> transports = transportRepository.findAvailableInArea(
                lat - latRange, lat + latRange,
                lng - lngRange, lng + lngRange
        );

        return transports.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransportResponseDTO> findTransportsByType(
            TransportType type, Double lat, Double lng, Double radiusKm
    ) {
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        List<Transport> transports = transportRepository.findAvailableByTypeInArea(
                type, lat - latRange, lat + latRange, lng - lngRange, lng + lngRange
        );

        return transports.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransportResponseDTO getTransportById(Long id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new TransportNotFoundException("Transport not found"));
        return toResponseDTO(transport);
    }

    @Transactional(readOnly = true)
    public List<TransportResponseDTO> findAvailableTransportsByType(TransportType type) {
        List<Transport> transports = transportRepository.findAvailableByType(type);
        return transports.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getAvailabilityStats() {
        Map<String, Long> stats = new HashMap<>();

        for (TransportType type : TransportType.values()) {
            long count = transportRepository.countAvailableByType(type);
            stats.put(type.name(), count);
        }

        return stats;
    }

    public TransportResponseDTO updateTransportStatus(Long transportId, String statusName) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new TransportNotFoundException("Transport not found"));

        TransportStatus status = statusRepository.findByName(statusName)
                .orElseThrow(() -> new DataNotFoundException("Status not found"));

        transport.setStatusId(status.getId());
        transport = transportRepository.save(transport);

        return toResponseDTO(transport);
    }

    private TransportResponseDTO toResponseDTO(Transport transport) {
        String statusName = statusRepository.findById(transport.getStatusId())
                .map(TransportStatus::getName).orElse("UNKNOWN");
        String cityName = transport.getCityId() != null ?
                cityRepository.findById(transport.getCityId()).map(City::getName).orElse(null) : null;
        return transportMapper.toResponseDTO(transport, statusName, cityName);
    }
}