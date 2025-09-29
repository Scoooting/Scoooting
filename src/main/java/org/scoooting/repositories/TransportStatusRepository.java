package org.scoooting.repositories;

import org.scoooting.entities.TransportStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransportStatusRepository extends CrudRepository<TransportStatus, Long> {
    Optional<TransportStatus> findByName(String name);
}
