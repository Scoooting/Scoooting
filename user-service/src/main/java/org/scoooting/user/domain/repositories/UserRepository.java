package org.scoooting.user.domain.repositories;

import org.scoooting.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findUsersWithFilters(String email, String name, int limit, int offset);

    long countUsersWithFilters(String email, String name);

}
