package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.BikeDTO;
import org.scoooting.dto.MotorcycleDTO;
import org.scoooting.dto.ScootersDTO;
import org.scoooting.dto.TransportDTO;
import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.*;
import org.scoooting.mappers.TransportMapper;
import org.scoooting.repositories.TransportRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportMapper transportMapper;
    private final ScooterService scooterService;
    private final BikeService bikeService;
    private final MotorcycleService motorcycleService;

    /**
     * Find all nearest transports (aggregates from all types)
     */
    public List<TransportDTO> findNearestTransports(float lat, float lon) {
        List<TransportDTO> allTransports = new ArrayList<>();

        // Get scooters and convert to TransportDTO
        List<ScootersDTO> scooters = scooterService.findNearestScooters(lat, lon);
        allTransports.addAll(scooters.stream()
                .map(this::scooterToTransport)
                .collect(Collectors.toList()));

        // Get bikes and convert to TransportDTO
        List<BikeDTO> bikes = bikeService.findNearestBikes(lat, lon, 2000);
        allTransports.addAll(bikes.stream()
                .map(this::bikeToTransport)
                .collect(Collectors.toList()));

        // Get motorcycles and convert to TransportDTO
        List<MotorcycleDTO> motorcycles = motorcycleService.findNearestMotorcycles(lat, lon, 2000);
        allTransports.addAll(motorcycles.stream()
                .map(this::motorcycleToTransport)
                .collect(Collectors.toList()));

        return allTransports;
    }

    /**
     * Find nearest transports by specific type
     */
    public List<TransportDTO> findNearestTransportsByType(float lat, float lon, int radius, TransportType type) {
        return switch (type) {
            case SCOOTER -> scooterService.findNearestScooters(lat, lon).stream()
                    .map(this::scooterToTransport)
                    .collect(Collectors.toList());
            case BICYCLE -> bikeService.findNearestBikes(lat, lon, radius).stream()
                    .map(this::bikeToTransport)
                    .collect(Collectors.toList());
            case MOTORCYCLE -> motorcycleService.findNearestMotorcycles(lat, lon, radius).stream()
                    .map(this::motorcycleToTransport)
                    .collect(Collectors.toList());
            default -> throw new IllegalArgumentException("Transport type not supported: " + type);
        };
    }

    /**
     * Get transport by ID and type
     */
    public TransportDTO getTransportById(Long id, TransportType type) {
        return switch (type) {
            case SCOOTER -> {
                ScootersDTO scooter = scooterService.findScooterById(id);
                yield scooterToTransport(scooter);
            }
            case BICYCLE -> {
                BikeDTO bike = bikeService.findBikeById(id);
                yield bikeToTransport(bike);
            }
            case MOTORCYCLE -> {
                MotorcycleDTO motorcycle = motorcycleService.findMotorcycleById(id);
                yield motorcycleToTransport(motorcycle);
            }
            default -> throw new IllegalArgumentException("Transport type not supported: " + type);
        };
    }

    /**
     * Find transports in city (aggregates all types)
     */
    public List<TransportDTO> findTransportsInCity(String city, int offset, int limit) {
        // For simplicity, get scooters from city (bikes/motorcycles would need similar methods)
        List<TransportDTO> cityTransports = new ArrayList<>();

        List<ScootersDTO> scooters = scooterService.findScootersInCity(city, offset, limit);
        cityTransports.addAll(scooters.stream()
                .map(this::scooterToTransport)
                .collect(Collectors.toList()));

        return cityTransports;
    }

    /**
     * Find available transports by type
     */
    public List<TransportDTO> findAvailableTransportsByType(TransportType type) {
        return switch (type) {
            case SCOOTER -> scooterService.findAvailableScooters().stream()
                    .map(this::scooterToTransport)
                    .collect(Collectors.toList());
            case BICYCLE -> bikeService.findAvailableBikes().stream()
                    .map(this::bikeToTransport)
                    .collect(Collectors.toList());
            case MOTORCYCLE -> motorcycleService.findAvailableMotorcycles().stream()
                    .map(this::motorcycleToTransport)
                    .collect(Collectors.toList());
            default -> throw new IllegalArgumentException("Transport type not supported: " + type);
        };
    }

    /**
     * Get availability stats for all transport types
     */
    public Map<TransportType, Long> getAvailabilityStats() {
        Map<TransportType, Long> stats = new HashMap<>();

        stats.put(TransportType.SCOOTER, scooterService.getAvailableCount());
        stats.put(TransportType.BICYCLE, bikeService.getAvailableCount());
        stats.put(TransportType.MOTORCYCLE, motorcycleService.getAvailableCount());

        return stats;
    }

    // Converter methods
    private TransportDTO scooterToTransport(ScootersDTO scooter) {
        return TransportDTO.builder()
                .id(scooter.id())
                .model(scooter.model())
                .type(TransportType.SCOOTER)
                .status(mapScooterStatus(scooter.status()))
                .latitude(scooter.latitude())
                .longitude(scooter.longitude())
                .build();
    }

    private TransportDTO bikeToTransport(BikeDTO bike) {
        return TransportDTO.builder()
                .id(bike.id())
                .model(bike.model())
                .type(bike.isElectric() ? TransportType.E_BIKE : TransportType.BICYCLE)
                .status(mapBikeStatus(bike.status()))
                .latitude(bike.latitude())
                .longitude(bike.longitude())
                .build();
    }

    private TransportDTO motorcycleToTransport(MotorcycleDTO motorcycle) {
        return TransportDTO.builder()
                .id(motorcycle.id())
                .model(motorcycle.model())
                .type(TransportType.MOTORCYCLE)
                .status(mapMotorcycleStatus(motorcycle.status()))
                .latitude(motorcycle.latitude())
                .longitude(motorcycle.longitude())
                .build();
    }

    // Status mapping methods
    private TransportStatus mapScooterStatus(ScootersStatus status) {
        return switch (status) {
            case FREE -> TransportStatus.AVAILABLE;
            case BUSY -> TransportStatus.IN_USE;
            case NONACTIVE, MAINTENANCE, LOW_BATTERY, DAMAGED -> TransportStatus.UNAVAILABLE;
        };
    }

    private TransportStatus mapBikeStatus(BikeStatus status) {
        return switch (status) {
            case FREE -> TransportStatus.AVAILABLE;
            case BUSY -> TransportStatus.IN_USE;
            case NONACTIVE, MAINTENANCE, FLAT_TIRE -> TransportStatus.UNAVAILABLE;
        };
    }

    private TransportStatus mapMotorcycleStatus(MotorcycleStatus status) {
        return switch (status) {
            case FREE -> TransportStatus.AVAILABLE;
            case BUSY -> TransportStatus.IN_USE;
            case NONACTIVE, MAINTENANCE, LOW_FUEL, ENGINE_FAULT -> TransportStatus.UNAVAILABLE;
        };
    }
}