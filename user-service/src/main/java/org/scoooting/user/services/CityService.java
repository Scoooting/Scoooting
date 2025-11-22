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

    /**
     * Get city name by ID.
     *
     * TRANSACTION NOT NEEDED:
     * - Single SELECT query: SELECT name FROM cities WHERE id = ?
     * - Read-only operation, no data modifications
     * - No related queries that need consistency
     * - Query is fast (indexed primary key lookup)
     *
     * WHY transaction would add only overhead:
     * - Opening transaction: BEGIN + COMMIT adds ~1-2ms
     * - Simple SELECT already runs in auto-commit mode (implicit transaction)
     * - No benefit from connection reuse (only one query)
     * - No benefit from isolation (single atomic read)
     *
     * WHEN transaction would be needed:
     * - If this method made multiple queries
     * - If called in a loop without batch loading
     * - If caller needs consistent snapshot across multiple calls
     *
     * Current implementation is optimal for single city lookup.
     *
     * @param id city ID
     * @return city name
     * @throws DataNotFoundException if city doesn't exist
     */
    public String getCityById(Long id) {
        Optional<City> optionalCity = cityRepository.findById(id);
        if (optionalCity.isPresent()) {
            return optionalCity.get().getName();
        }

        throw new DataNotFoundException("City not found");
    }
}
