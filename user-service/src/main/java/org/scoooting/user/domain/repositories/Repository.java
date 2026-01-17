package org.scoooting.user.domain.repositories;

import java.util.Optional;

public interface Repository<T, ID> {

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    T save(T entity);

    void delete(T entity);

    void deleteById(ID id);

}
