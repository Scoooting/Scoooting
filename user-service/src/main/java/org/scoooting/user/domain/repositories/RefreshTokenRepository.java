package org.scoooting.user.domain.repositories;

import org.scoooting.user.domain.model.RefreshToken;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends Repository<RefreshToken, Long> {

    void insert(@Param("userId") Long userId, @Param("token") String token);

    void update(@Param("userId") Long userId, @Param("token") String token);

}
