package org.scoooting.user.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.entities.City;
import org.scoooting.user.exceptions.common.DataNotFoundException;
import org.scoooting.user.repositories.CityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public String getCityById(Long id) {
        Optional<City> optionalCity = cityRepository.findById(id);
        if (optionalCity.isPresent()) {
            return optionalCity.get().getName();
        }

        throw new DataNotFoundException("City not found");
    }
}
