package org.scoooting.rental.adapters.persistence.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.persistence.entities.RentalStatusEntity;
import org.scoooting.rental.adapters.persistence.mappers.RentalStatusEntityMapper;
import org.scoooting.rental.adapters.persistence.repositories.jdbc.RentalStatusJdbcRepository;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RentalStatusRepositoryImpl implements RentalStatusRepository {

    private final RentalStatusJdbcRepository repository;
    private final RentalStatusEntityMapper mapper;

    @Override
    public Optional<RentalStatus> findByName(String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public Optional<RentalStatus> findById(Long aLong) {
        return repository.findById(aLong).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public RentalStatus save(RentalStatus entity) {
        RentalStatusEntity saved = mapper.toEntity(entity);
        return mapper.toDomain(repository.save(saved));
    }

    @Override
    public void delete(RentalStatus entity) {
        repository.delete(mapper.toEntity(entity));
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }
}
