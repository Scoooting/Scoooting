package org.scoooting.user.repositories;

import org.scoooting.user.entities.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT * FROM users\s
        WHERE (:email IS NULL OR email LIKE CONCAT('%', :email, '%'))\s
        AND (:name IS NULL OR name LIKE CONCAT('%', :name, '%'))
        ORDER BY id
        LIMIT :limit OFFSET :offset
       \s""")
    List<User> findUsersWithFilters(String email, String name, int limit, int offset);

    @Query("""
        SELECT COUNT(*) FROM users\s
        WHERE (:email IS NULL OR email LIKE CONCAT('%', :email, '%'))\s
        AND (:name IS NULL OR name LIKE CONCAT('%', :name, '%'))
       \s""")
    long countUsersWithFilters(String email, String name);
}
