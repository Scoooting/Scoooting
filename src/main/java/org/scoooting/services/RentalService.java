package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.*;
import org.scoooting.entities.Rental;
import org.scoooting.entities.Scooter;
import org.scoooting.entities.Transport;
import org.scoooting.entities.User;
import org.scoooting.entities.enums.RentalStatus;
import org.scoooting.entities.enums.ScootersStatus;
import org.scoooting.entities.enums.TransportStatus;
import org.scoooting.mappers.RentalMapper;
import org.scoooting.repositories.RentalRepository;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.repositories.TransportRepository;
import org.scoooting.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {

    private final TransportRepository transportRepository;
    private final RentalRepository rentalRepository;
    private final ScooterRepository scooterRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;

    private static final BigDecimal BASE_RATE = new BigDecimal("0.50"); // 50 cents per minute
    private static final BigDecimal UNLOCK_FEE = new BigDecimal("1.00"); // $1 unlock fee
    private static final int MAX_RENTAL_HOURS = 24; // Maximum rental duration

    /**
     * Start a new rental - Complex transaction handling multiple entities
     */
    @Transactional
    public RentalDTO startRental(Long userId, Long transportId, Float startLat, Float startLon) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (rentalRepository.findActiveRentalByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has an active rental");
        }

        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("Transport not found"));

        if (transport.getStatus() != TransportStatus.AVAILABLE) {
            throw new IllegalStateException("Transport is not available for rental");
        }

        // Create rental with transport type
        Rental rental = new Rental(userId, transportId, transport.getType(), startLat, startLon);
        rental = rentalRepository.save(rental);

        // Update transport status
        transport.setStatus(TransportStatus.IN_USE);
        transportRepository.save(transport);

        return rentalMapper.toDTO(rental);
    }

    /**
     * End rental - Complex calculation and multi-entity update
     */
    @Transactional
    public RentalDTO endRental(Long userId, Float endLat, Float endLon) {
        // Find active rental
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found for user"));

        // Calculate duration and cost
        LocalDateTime endTime = LocalDateTime.now();
        long minutes = Duration.between(rental.getStartTime(), endTime).toMinutes();

        if (minutes > MAX_RENTAL_HOURS * 60)
            throw new IllegalStateException("Rental exceeded maximum duration");

        BigDecimal totalCost = UNLOCK_FEE.add(BASE_RATE.multiply(BigDecimal.valueOf(minutes)));

        // Update rental
        rental.setEndTime(endTime);
        rental.setEndLatitude(endLat);
        rental.setEndLongitude(endLon);
        rental.setDurationMinutes((int) minutes);
        rental.setTotalCost(totalCost);
        rental.setStatus(RentalStatus.COMPLETED);
        rental = rentalRepository.save(rental);

        // Free up transport and update its location
        Transport transport = transportRepository.findById(rental.getId())
                .orElseThrow(() -> new IllegalStateException("Transport not found"));
        transport.setStatus(TransportStatus.AVAILABLE);
        transport.setLatitude(endLat);
        transport.setLongitude(endLon);
        transportRepository.save(transport);

        // Award bonus points (1 point per minute)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setBonuses(user.getBonuses() + (int) minutes);
        userRepository.save(user);

        return rentalMapper.toDTO(rental);
    }

    /**
     * Cancel active rental
     */
    @Transactional
    public void cancelRental(Long userId) {
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found"));

        rental.setStatus(RentalStatus.CANCELLED);
        rental.setEndTime(LocalDateTime.now());
        rentalRepository.save(rental);

        // Free transport
        Transport transport = transportRepository.findById(rental.getId())
                .orElseThrow(() -> new IllegalStateException("Transport not found"));
        transport.setStatus(TransportStatus.AVAILABLE);
        transportRepository.save(transport);
    }

    /**
     * Get user rental history with pagination
     */
    @Transactional(readOnly = true)
    public PaginatedRentalsDTO getUserRentalHistory(Long userId, int offset, int limit) {
        List<Rental> rentals = rentalRepository.findRentalHistoryByUserId(userId, offset, limit);
        Long totalCount = rentalRepository.countRentalsByUserId(userId);

        List<RentalDTO> rentalDTOs = rentals.stream()
                .map(rentalMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedRentalsDTO(rentalDTOs, totalCount);
    }

    /**
     * Get current active rental for user
     */
    @Transactional(readOnly = true)
    public Optional<RentalDTO> getActiveRental(Long userId) {
        return rentalRepository.findActiveRentalByUserId(userId)
                .map(rentalMapper::toDTO);
    }

    /**
     * COMPLEX QUERY 1: Get top users analytics
     */
    @Transactional(readOnly = true)
    public List<UserAnalyticsDTO> getTopUsersByUsage(LocalDateTime startDate, LocalDateTime endDate,
                                                     int minRentals, int limit) {
        List<Map<String, Object>> results = rentalRepository.findTopUsersByUsageInPeriod(
                startDate, endDate, minRentals, limit);

        return results.stream()
                .map(this::mapToUserAnalyticsDTO)
                .collect(Collectors.toList());
    }

    /**
     * COMPLEX QUERY 2: Get scooter utilization analytics
     */
    @Transactional(readOnly = true)
    public List<TransportAnalyticsDTO> getTransportUtilizationInArea(Double centerLat, Double centerLon,
                                                                 Double radiusMeters, LocalDateTime startDate) {
        List<Map<String, Object>> results = rentalRepository.findTransportUtilizationInArea(
                centerLat, centerLon, radiusMeters, startDate);

        return results.stream()
                .map(this::mapToTransportAnalyticsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get revenue by city for business analytics
     */
//    @Transactional(readOnly = true)
//    public List<CityRevenueDTO> getRevenueByCity(LocalDateTime startDate, LocalDateTime endDate) {
//        List<Map<String, Object>> results = rentalRepository.findRevenueByCity(startDate, endDate);
//
//        return results.stream()
//                .map(this::mapToCityRevenueDTO)
//                .collect(Collectors.toList());
//    }

    /**
     * Get scooters needing maintenance
     */
    @Transactional(readOnly = true)
    public List<MaintenanceAlertDTO> getTransportsNeedingMaintenance(int maxRentals, int maxMinutes) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(30); // Last 30 days
        List<Map<String, Object>> results = rentalRepository.findTransportsNeedingMaintenance(
                sinceDate, maxRentals, maxMinutes);

        return results.stream()
                .map(this::mapToMaintenanceAlertDTO)
                .collect(Collectors.toList());
    }

    // Mapping helper methods
    private UserAnalyticsDTO mapToUserAnalyticsDTO(Map<String, Object> row) {
        return UserAnalyticsDTO.builder()
                .userId(((Number) row.get("id")).longValue())
                .email((String) row.get("email"))
                .name((String) row.get("name"))
                .rentalCount(((Number) row.get("rental_count")).intValue())
                .totalSpent(new BigDecimal(row.get("total_spent").toString()))
                .avgDuration(((Number) row.get("avg_duration")).doubleValue())
                .build();
    }

    private TransportAnalyticsDTO mapToTransportAnalyticsDTO(Map<String, Object> row) {
        return TransportAnalyticsDTO.builder()
                .transportId(((Number) row.get("id")).longValue())
                .model((String) row.get("model"))
                .transportType((String) row.get("transport_type"))
                .latitude(((Number) row.get("latitude")).doubleValue())
                .longitude(((Number) row.get("longitude")).doubleValue())
                .totalRentals(((Number) row.get("total_rentals")).intValue())
                .totalMinutesUsed(((Number) row.get("total_minutes_used")).intValue())
                .totalRevenue(new BigDecimal(row.get("total_revenue").toString()))
                .avgRentalDuration(((Number) row.get("avg_rental_duration")).doubleValue())
                .lastRentalTime((LocalDateTime) row.get("last_rental_time"))
                .usageCategory((String) row.get("usage_category"))
                .build();
    }

    private ScooterAnalyticsDTO mapToScooterAnalyticsDTO(Map<String, Object> row) {
        return ScooterAnalyticsDTO.builder()
                .scooterId(((Number) row.get("id")).longValue())
                .model((String) row.get("model"))
                .latitude(((Number) row.get("latitude")).doubleValue())
                .longitude(((Number) row.get("longitude")).doubleValue())
                .totalRentals(((Number) row.get("total_rentals")).intValue())
                .totalMinutesUsed(((Number) row.get("total_minutes_used")).intValue())
                .totalRevenue(new BigDecimal(row.get("total_revenue").toString()))
                .avgRentalDuration(((Number) row.get("avg_rental_duration")).doubleValue())
                .lastRentalTime((LocalDateTime) row.get("last_rental_time"))
                .usageCategory((String) row.get("usage_category"))
                .build();
    }

    private CityRevenueDTO mapToCityRevenueDTO(Map<String, Object> row) {
        return CityRevenueDTO.builder()
                .cityName((String) row.get("city_name"))
                .totalRentals(((Number) row.get("total_rentals")).intValue())
                .totalRevenue(new BigDecimal(row.get("total_revenue").toString()))
                .avgDuration(((Number) row.get("avg_duration")).doubleValue())
                .build();
    }

    private MaintenanceAlertDTO mapToMaintenanceAlertDTO(Map<String, Object> row) {
        return MaintenanceAlertDTO.builder()
                .scooterId(((Number) row.get("id")).longValue())
                .model((String) row.get("model"))
                .latitude(((Number) row.get("latitude")).doubleValue())
                .longitude(((Number) row.get("longitude")).doubleValue())
                .rentalCount(((Number) row.get("rental_count")).intValue())
                .totalUsageMinutes(((Number) row.get("total_usage_minutes")).intValue())
                .build();
    }
}
