package org.scoooting.transport.repositories;

import org.scoooting.transport.entities.GasMotorcycle;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GasMotorcycleRepository extends CrudRepository<GasMotorcycle, Long> {
}