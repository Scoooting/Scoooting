package org.scoooting.transport.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.clients.UserClient;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportStatusRepository statusRepository;
//    private final CityRepository cityRepository; TODO
    private final TransportMapper transportMapper;
    private final UserClient userClient;

    @Transactional(readOnly = true)
    public List<TransportResponseDTO> findNearestTransports(Double lat, Double lng, Double radiusKm) {
        // Calculate boundaries
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        List<Transport> transports = transportRepository.findAvailableInArea(
                lat - latRange, lat + latRange,
                lng - lngRange, lng + lngRange
        );

        return transportMapper.toResponseDTOList(transports);
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

        return transports.stream().map(this::toResponseDTO).toList();
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
        return transports.stream().map(this::toResponseDTO).toList();
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

    public Long getStatusId(String name) {
        Optional<TransportStatus> optionalTransportStatus = statusRepository.findByName(name);
        if (optionalTransportStatus.isPresent())
            return optionalTransportStatus.get().getId();

        throw new DataNotFoundException("Status not found");
    }

    public void updateCoordinates(UpdateCoordinatesDTO updateCoordinatesDTO) {
        Transport transport = transportRepository.findById(updateCoordinatesDTO.transportId()).orElseThrow();
        transport.setLatitude(updateCoordinatesDTO.latitude());
        transport.setLongitude(updateCoordinatesDTO.latitude());

        transportRepository.save(transport);
    }

    public TransportResponseDTO toResponseDTO(Transport transport) {
        String statusName = statusRepository.findById(transport.getStatusId())
                .map(TransportStatus::getName).orElse("UNKNOWN");
        String cityName = transport.getCityId() != null ? userClient.getCityById(transport.getCityId()).getBody() : null;
        return transportMapper.toResponseDTO(transport, statusName, cityName);
    } // TODO
}