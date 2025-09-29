package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.common.PageResponseDTO;
import org.scoooting.dto.response.RentalResponseDTO;
import org.scoooting.entities.*;
import org.scoooting.exceptions.common.DataNotFoundException;
import org.scoooting.exceptions.transport.TransportNotFoundException;
import org.scoooting.exceptions.user.UserNotFoundException;
import org.scoooting.mappers.RentalMapper;
import org.scoooting.repositories.*;
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

    private final RentalRepository rentalRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final TransportStatusRepository transportStatusRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final RentalMapper rentalMapper;

    private static final BigDecimal BASE_RATE = new BigDecimal("0.50"); // 50 cents per minute
    private static final BigDecimal UNLOCK_FEE = new BigDecimal("1.00"); // $1 unlock fee

    @Transactional
    public RentalResponseDTO startRental(Long userId, Long transportId, Double startLat, Double startLng) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }

        // Check if user already has active rental
        if (rentalRepository.findActiveRentalByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has an active rental");
        }

        // Validate transport
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new TransportNotFoundException("Transport not found"));

        // Check if transport is available
        TransportStatus availableStatus = transportStatusRepository.findByName("AVAILABLE")
                .orElseThrow(() -> new DataNotFoundException("AVAILABLE status not found"));

        if (!transport.getStatusId().equals(availableStatus.getId())) {
            throw new IllegalStateException("Transport is not available for rental");
        }

        // Create rental
        Rental rental = new Rental();
        rental.setUserId(userId);
        rental.setTransportId(transportId);
        rental.setStartLatitude(startLat);
        rental.setStartLongitude(startLng);
        rental.setStartTime(LocalDateTime.now());

        // Set ACTIVE status
        RentalStatus activeStatus = rentalStatusRepository.findByName("ACTIVE")
                .orElseThrow(() -> new DataNotFoundException("ACTIVE status not found"));
        rental.setStatusId(activeStatus.getId());

        rental = rentalRepository.save(rental);

        // Update transport to IN_USE
        TransportStatus inUseStatus = transportStatusRepository.findByName("IN_USE")
                .orElseThrow(() -> new DataNotFoundException("IN_USE status not found"));
        transport.setStatusId(inUseStatus.getId());
        transportRepository.save(transport);

        return toResponseDTO(rental);
    }

    @Transactional
    public RentalResponseDTO endRental(Long userId, Double endLat, Double endLng) {
        // Find active rental
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found for user"));

        // Calculate duration and cost
        LocalDateTime endTime = LocalDateTime.now();
        long minutes = Duration.between(rental.getStartTime(), endTime).toMinutes();
        BigDecimal totalCost = UNLOCK_FEE.add(BASE_RATE.multiply(BigDecimal.valueOf(minutes)));

        // Calculate distance (simple Euclidean distance)
        double distance = calculateDistance(
                rental.getStartLatitude(), rental.getStartLongitude(),
                endLat, endLng
        );

        // Update rental
        rental.setEndTime(endTime);
        rental.setEndLatitude(endLat);
        rental.setEndLongitude(endLng);
        rental.setDurationMinutes((int) minutes);
        rental.setTotalCost(totalCost);
        rental.setDistanceKm(BigDecimal.valueOf(distance));

        // Set COMPLETED status
        RentalStatus completedStatus = rentalStatusRepository.findByName("COMPLETED")
                .orElseThrow(() -> new DataNotFoundException("COMPLETED status not found"));
        rental.setStatusId(completedStatus.getId());

        rental = rentalRepository.save(rental);

        // Update transport
        Transport transport = transportRepository.findById(rental.getTransportId())
                .orElseThrow(() -> new TransportNotFoundException("Transport not found"));

        TransportStatus availableStatus = transportStatusRepository.findByName("AVAILABLE")
                .orElseThrow(() -> new DataNotFoundException("AVAILABLE status not found"));
        transport.setStatusId(availableStatus.getId());
        transport.setLatitude(endLat);
        transport.setLongitude(endLng);
        transportRepository.save(transport);

        // Award bonus points
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBonuses(user.getBonuses() + (int) minutes);
        userRepository.save(user);

        return toResponseDTO(rental);
    }

    @Transactional
    public void cancelRental(Long userId) {
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found"));

        // Set CANCELLED status
        RentalStatus cancelledStatus = rentalStatusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new DataNotFoundException("CANCELLED status not found"));
        rental.setStatusId(cancelledStatus.getId());
        rental.setEndTime(LocalDateTime.now());
        rentalRepository.save(rental);

        // Free transport
        Transport transport = transportRepository.findById(rental.getTransportId())
                .orElseThrow(() -> new TransportNotFoundException("Transport not found"));

        TransportStatus availableStatus = transportStatusRepository.findByName("AVAILABLE")
                .orElseThrow(() -> new DataNotFoundException("AVAILABLE status not found"));
        transport.setStatusId(availableStatus.getId());
        transportRepository.save(transport);
    }

    @Transactional(readOnly = true)
    public Optional<RentalResponseDTO> getActiveRental(Long userId) {
        return rentalRepository.findActiveRentalByUserId(userId)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<RentalResponseDTO> getUserRentalHistory(
            Long userId, int page, int size
    ) {
        int offset = page * size;
        List<Rental> rentals = rentalRepository.findRentalHistoryByUserId(userId, offset, size);
        long total = rentalRepository.countRentalsByUserId(userId);

        List<RentalResponseDTO> rentalDTOs = rentals.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(rentalDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    private RentalResponseDTO toResponseDTO(Rental rental) {
        // Get user name
        String userName = userRepository.findById(rental.getUserId())
                .map(User::getName).orElse("Unknown User");

        // Get transport type
        String transportType = transportRepository.findById(rental.getTransportId())
                .map(t -> t.getTransportType().name()).orElse("UNKNOWN");

        // Get status name
        String statusName = rentalStatusRepository.findById(rental.getStatusId())
                .map(RentalStatus::getName).orElse("UNKNOWN");

        return rentalMapper.toResponseDTO(rental, userName, transportType, statusName);
    }

    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 0.0;
        }

        // Simple Euclidean distance in km (rough approximation)
        double latDiff = Math.abs(lat1 - lat2) * 111.0; // 1 degree ≈ 111 km
        double lngDiff = Math.abs(lng1 - lng2) * 111.0 * Math.cos(Math.toRadians(lat1));
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }
}