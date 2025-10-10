package org.scoooting.transport.repositories;

import org.scoooting.transport.entities.ElectricScooter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScooterRepository extends CrudRepository<ElectricScooter, Long> {
}
