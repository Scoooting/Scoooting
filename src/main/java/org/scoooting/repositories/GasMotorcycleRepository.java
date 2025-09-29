package org.scoooting.repositories;

import org.scoooting.entities.Motorcycle;
import org.scoooting.entities.enums.MotorcycleStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GasMotorcycleRepository extends CrudRepository<Motorcycle, Long> {
}