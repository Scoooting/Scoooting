package org.scoooting.user.adapters.persistence.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.adapters.persistence.entities.UserRoleEntity;
import org.scoooting.user.adapters.persistence.mappers.UserRoleEntityMapper;
import org.scoooting.user.adapters.persistence.repositories.jdbc.UserRoleJdbcRepository;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.UserRoleRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleJdbcRepository repository;
    private final UserRoleEntityMapper mapper;

    @Override
    public Optional<UserRole> findById(Long aLong) {
        return repository.findById(aLong).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public UserRole save(UserRole entity) {
        UserRoleEntity saved = mapper.toEntity(entity);
        return mapper.toDomain(repository.save(saved));
    }

    @Override
    public void delete(UserRole entity) {
        repository.delete(mapper.toEntity(entity));
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }

    @Override
    public Optional<UserRole> findByName(String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }
}
