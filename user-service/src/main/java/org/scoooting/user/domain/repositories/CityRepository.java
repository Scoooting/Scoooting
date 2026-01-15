package org.scoooting.user.domain.repositories;

import org.scoooting.user.domain.model.City;

import java.util.Optional;

public interface CityRepository extends Repository<City, Long> {
    Optional<City> findByName(String name);
}
