package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.ScootersDTO;
import org.scoooting.entities.enums.ScootersStatus;
import org.scoooting.mappers.ScooterMapper;
import org.scoooting.repositories.ScooterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;
    private final ScooterMapper scooterMapper;

    public List<ScootersDTO> findNearestScooters(float lat, float lon) {
        return scooterRepository.getNearestScooters(lat, lon).stream()
                .map(scooterMapper::toDTO).collect(Collectors.toList());
    }

    public List<ScootersDTO> findScootersInCity(String city, int offset, int limit) {
        return scooterRepository.getScootersInCity(city, offset, limit).stream()
                .map(scooterMapper::toDTO).collect(Collectors.toList());
    }

    public ScootersDTO findScooterById(Long id) {
        return scooterRepository.findById(id)
                .map(scooterMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Scooter not found"));
    }

    public Long getAvailableCount() {
        return scooterRepository.countByStatus(ScootersStatus.FREE);
    }

    public List<ScootersDTO> findAvailableScooters() {
        return scooterRepository.findByStatus(ScootersStatus.FREE).stream()
                .map(scooterMapper::toDTO)
                .collect(Collectors.toList());
    }
}
