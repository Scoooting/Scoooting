package org.scoooting.user.adapters.persistence.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.adapters.persistence.entities.UserEntity;
import org.scoooting.user.adapters.persistence.mappers.UserEntityMapper;
import org.scoooting.user.adapters.persistence.repositories.jdbc.UserJdbcRepository;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.repositories.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJdbcRepository repository;
    private final UserEntityMapper mapper;


    @Override
    public Optional<User> findById(Long aLong) {
        return repository.findById(aLong).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public User save(User entity) {
        UserEntity saved = mapper.toEntity(entity);
        return mapper.toDomain(repository.save(saved));
    }

    @Override
    public void delete(User entity) {
        repository.delete(mapper.toEntity(entity));
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public List<User> findUsersWithFilters(String email, String name, int limit, int offset) {
        return mapper.toDomainList(repository.findUsersWithFilters(email, name, limit, offset));
    }

    @Override
    public long countUsersWithFilters(String email, String name) {
        return repository.countUsersWithFilters(email, name);
    }
}
