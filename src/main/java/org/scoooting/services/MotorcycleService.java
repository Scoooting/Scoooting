package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.MotorcycleDTO;
import org.scoooting.entities.enums.MotorcycleStatus;
import org.scoooting.mappers.MotorcycleMapper;
import org.scoooting.repositories.MotorcycleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MotorcycleService {

    private final MotorcycleRepository motorcycleRepository;
    private final MotorcycleMapper motorcycleMapper;

    public List<MotorcycleDTO> findNearestMotorcycles(float lat, float lon, int radius) {
        return motorcycleRepository.getNearestMotorcycles(lat, lon, radius).stream()
                .map(motorcycleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MotorcycleDTO findMotorcycleById(Long id) {
        return motorcycleRepository.findById(id)
                .map(motorcycleMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Motorcycle not found"));
    }

    public List<MotorcycleDTO> findAvailableMotorcycles() {
        return motorcycleRepository.findByStatus(MotorcycleStatus.FREE).stream()
                .map(motorcycleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Long getAvailableCount() {
        return motorcycleRepository.countAvailable();
    }
}