package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.ScootersDto;
import org.scoooting.mappers.ScooterMapper;
import org.scoooting.repositories.ScooterRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;
    private final ScooterMapper scooterMapper;

    public List<ScootersDto> findNearestScooters(float lat, float lon) {
        return scooterRepository.getNearestScooters(lat, lon).stream()
                .map(scooterMapper::toDto).collect(Collectors.toList());
    }

    public List<ScootersDto> findScootersInCity(String city, int offset, int limit) {
        return scooterRepository.getScootersInCity(city, offset, limit).stream()
                .map(scooterMapper::toDto).collect(Collectors.toList());
    }
}
