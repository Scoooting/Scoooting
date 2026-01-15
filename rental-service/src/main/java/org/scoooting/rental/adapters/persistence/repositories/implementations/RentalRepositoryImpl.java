package org.scoooting.rental.adapters.persistence.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.persistence.entities.RentalEntity;
import org.scoooting.rental.adapters.persistence.mappers.RentalEntityMapper;
import org.scoooting.rental.adapters.persistence.repositories.jdbc.RentalJdbcRepository;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RentalRepositoryImpl implements RentalRepository {

    private final RentalJdbcRepository repository;
    private final RentalEntityMapper mapper;

    @Override
    public Optional<Rental> findActiveRentalByUserId(Long userId) {
        return repository.findActiveRentalByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<Rental> findRentalHistoryByUserId(Long userId, int offset, int limit) {
        return mapper.toDomainList(repository.findRentalHistoryByUserId(userId, offset, limit));
    }

    @Override
    public List<Rental> findAllRentals(int offset, int limit) {
        return mapper.toDomainList(repository.findAllRentals(offset, limit));
    }

    @Override
    public long countAllRentals() {
        return repository.countAllRentals();
    }

    @Override
    public long countRentalsByUserId(Long userId) {
        return repository.countRentalsByUserId(userId);
    }

    @Override
    public List<Rental> findByStatusId(Long statusId) {
        return null;
    }

    @Override
    public Optional<Rental> findById(Long aLong) {
        return repository.findById(aLong).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public Rental save(Rental entity) {
        RentalEntity saved = mapper.toEntity(entity);
        return mapper.toDomain(repository.save(saved));
    }

    @Override
    public void delete(Rental entity) {
        repository.delete(mapper.toEntity(entity));
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }
}
