package org.scoooting.user.repositories;

import org.scoooting.user.entities.RefreshToken;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    @Modifying
    @Query("INSERT INTO refresh_tokens(user_id, token) VALUES(:userId, :token)")
    void insert(@Param("userId") Long userId, @Param("token") String token);

    @Modifying
    @Query("UPDATE refresh_tokens SET token = :token WHERE user_id = :userId")
    void update(@Param("userId") Long userId, @Param("token") String token);

}
