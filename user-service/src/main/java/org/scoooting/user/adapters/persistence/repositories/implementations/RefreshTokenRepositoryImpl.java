package org.scoooting.user.adapters.persistence.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.adapters.persistence.entities.RefreshTokenEntity;
import org.scoooting.user.adapters.persistence.mappers.RefreshTokenEntityMapper;
import org.scoooting.user.adapters.persistence.repositories.jdbc.RefreshTokenJdbcRepository;
import org.scoooting.user.domain.model.RefreshToken;
import org.scoooting.user.domain.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJdbcRepository repository;
    private final RefreshTokenEntityMapper mapper;

    @Override
    public void insert(Long userId, String token) {
        repository.insert(userId, token);
    }

    @Override
    public void update(Long userId, String token) {
        repository.update(userId, token);
    }

    @Override
    public Optional<RefreshToken> findById(Long aLong) {
        return repository.findById(aLong).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public RefreshToken save(RefreshToken entity) {
        RefreshTokenEntity saved = mapper.toEntity(entity);
        return mapper.toDomain(repository.save(saved));
    }

    @Override
    public void delete(RefreshToken entity) {
        repository.delete(mapper.toEntity(entity));
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }
}
