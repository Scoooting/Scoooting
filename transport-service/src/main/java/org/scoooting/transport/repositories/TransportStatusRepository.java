package org.scoooting.transport.repositories;

import org.scoooting.transport.entities.TransportStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransportStatusRepository extends CrudRepository<TransportStatus, Long> {
    Optional<TransportStatus> findByName(String name);
}
