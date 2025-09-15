package org.scoooting.repositories;

import org.scoooting.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);

    Page<User> findAll(Pageable pageable);

    Optional<User> findByEmail(String email);
}
