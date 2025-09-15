package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.BikeDTO;
import org.scoooting.entities.enums.BikeStatus;
import org.scoooting.mappers.BikeMapper;
import org.scoooting.repositories.BikeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BikeService {

    private final BikeRepository bikeRepository;
    private final BikeMapper bikeMapper;

    public List<BikeDTO> findNearestBikes(float lat, float lon, int radius) {
        return bikeRepository.getNearestBikes(lat, lon, radius).stream()
                .map(bikeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public BikeDTO findBikeById(Long id) {
        return bikeRepository.findById(id)
                .map(bikeMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Bike not found"));
    }

    public List<BikeDTO> findAvailableBikes() {
        return bikeRepository.findByStatus(BikeStatus.FREE).stream()
                .map(bikeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Long getAvailableCount() {
        return bikeRepository.countAvailable();
    }
}