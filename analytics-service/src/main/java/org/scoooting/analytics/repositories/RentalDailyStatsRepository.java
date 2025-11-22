package org.scoooting.analytics.repositories;

import org.scoooting.analytics.entities.RentalDailyStats;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RentalDailyStatsRepository extends CrudRepository<RentalDailyStats, LocalDate> {

    @Modifying
    @Query("insert into rental_daily_stats (date) values (:date)")
    void createNewDate(@Param("date") LocalDate date);
}
