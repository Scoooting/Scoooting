package org.scoooting.analytics.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoooting.analytics.dto.RentalResponseDTO;
import org.scoooting.analytics.entities.RentalDailyStats;
import org.scoooting.analytics.repositories.RentalDailyStatsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ObjectMapper mapper;
    private final RentalDailyStatsRepository rentalDailyStatsRepository;

    @KafkaListener(topics = "rentalStatsStart", groupId = "analytics")
    public void addStartRental(Map<String, Object> data) {
        RentalResponseDTO newRental = mapper.convertValue(data, RentalResponseDTO.class);

        LocalDate date = newRental.startTime().toLocalDate();
        RentalDailyStats todayStats = rentalDailyStatsRepository.findById(date).orElse(null);
        if (todayStats == null)
            rentalDailyStatsRepository.createNewDate(date);

        todayStats.setTotalStarts(todayStats.getTotalStarts() + 1);
        rentalDailyStatsRepository.save(todayStats);
    }

    @KafkaListener(topics = "rentalStatsStart", groupId = "analytics")
    public void updateRentalStats(Map<String, Object> data) {
        RentalResponseDTO newRental = mapper.convertValue(data, RentalResponseDTO.class);

        LocalDate date = newRental.endTime().toLocalDate();
        RentalDailyStats todayStats = rentalDailyStatsRepository.findById(date).orElse(null);
        if (todayStats == null)
            rentalDailyStatsRepository.createNewDate(date);

        todayStats.setTotalStarts(todayStats.getTotalStarts() + 1);
        rentalDailyStatsRepository.save(todayStats);
    }
}
