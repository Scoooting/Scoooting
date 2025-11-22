package org.scoooting.analytics.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.analytics.entities.RentalDailyStats;
import org.scoooting.analytics.repositories.RentalDailyStatsRepository;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final RentalDailyStatsRepository rentalDailyStatsRepository;

    @PutMapping("/insert")
    public void insert() {
        rentalDailyStatsRepository.save(new RentalDailyStats(LocalDate.now()));
    }
}
