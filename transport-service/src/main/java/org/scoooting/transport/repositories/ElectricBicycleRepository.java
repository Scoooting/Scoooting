package org.scoooting.transport.repositories;

import org.scoooting.transport.entities.ElectricBicycle;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricBicycleRepository extends CrudRepository<ElectricBicycle, Long> {
}