package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.ScootersDto;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.utils.Geography;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;

    public Double[] findNearestScootersInDistrict(String district) {
        return new Geography().notifyScooter(1);
    }
}
