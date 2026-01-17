package org.scoooting.user.adapters.persistence.repositories.jdbc;

import org.scoooting.user.adapters.persistence.entities.CityEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CityJdbcRepository extends CrudRepository<CityEntity, Long> {
    Optional<CityEntity> findByName(String name);
}
