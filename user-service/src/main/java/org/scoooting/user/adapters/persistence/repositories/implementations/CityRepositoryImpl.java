package org.scoooting.user.adapters.persistence.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.adapters.persistence.entities.CityEntity;
import org.scoooting.user.adapters.persistence.mappers.CityEntityMapper;
import org.scoooting.user.adapters.persistence.repositories.jdbc.CityJdbcRepository;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.repositories.CityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CityRepositoryImpl implements CityRepository {

    private final CityJdbcRepository repository;
    private final CityEntityMapper mapper;

    @Override
    public Optional<City> findByName(String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public Optional<City> findById(Long aLong) {
        return repository.findById(aLong).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public City save(City entity) {
        CityEntity saved = mapper.toEntity(entity);
        return mapper.toDomain(repository.save(saved));
    }

    @Override
    public void delete(City entity) {
        repository.delete(mapper.toEntity(entity));
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }
}
