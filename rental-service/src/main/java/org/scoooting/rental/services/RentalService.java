package org.scoooting.rental.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.clients.TransportClient;
import org.scoooting.rental.clients.UserClient;
import org.scoooting.rental.dto.common.PageResponseDTO;
import org.scoooting.rental.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.dto.request.UpdateUserRequestDTO;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.scoooting.rental.entities.Rental;
import org.scoooting.rental.entities.RentalStatus;
import org.scoooting.rental.exceptions.DataNotFoundException;
import org.scoooting.rental.mappers.RentalMapper;
import org.scoooting.rental.repositories.RentalRepository;
import org.scoooting.rental.repositories.RentalStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {

    private final RentalRepository rentalRepository;
    private final TransportClient transportClient;
    private final UserClient userClient;
    private final RentalStatusRepository rentalStatusRepository;
    private final RentalMapper rentalMapper;

    private static final BigDecimal BASE_RATE = new BigDecimal("0.50"); // 50 cents per minute
    private static final BigDecimal UNLOCK_FEE = new BigDecimal("1.00"); // $1 unlock fee

    @Transactional
    public RentalResponseDTO startRental(Long userId, Long transportId, Double startLat, Double startLng) {
        // Validate user exists
        userClient.getUserById(userId);

        // Validate transport
        transportClient.getTransport(transportId);

        RentalStatus rentalStatus = rentalStatusRepository.findByName("ACTIVE")
                .orElseThrow(() -> new DataNotFoundException("ACTIVE status not found"));

        // Create rental
        Rental rental = Rental.builder()
                .userId(userId)
                .transportId(transportId)
                .statusId(rentalStatus.getId())
                .startTime(LocalDateTime.now())
                .startLatitude(startLat)
                .startLongitude(startLng)
                .build();
        rental = rentalRepository.save(rental);
        transportClient.updateTransportStatus(transportId, "IN_USE");

        return rentalMapper.toResponseDTO(rental);
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
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId()).getBody();
        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");
        transportClient.updateTransportCoordinates(new UpdateCoordinatesDTO(transport.id(), endLat, endLng));

        // Award bonus points
        UserResponseDTO user = userClient.getUserById(userId).getBody();
        userClient.updateUser(userId, new UpdateUserRequestDTO(null, null,
                user.bonuses() + (int) minutes));

        return rentalMapper.toResponseDTO(rental);
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
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId()).getBody();
        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");
    }

    @Transactional(readOnly = true)
    public RentalResponseDTO getActiveRental(Long userId) {
        return rentalMapper.toResponseDTO(rentalRepository.findActiveRentalByUserId(userId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<RentalResponseDTO> getUserRentalHistory(Long userId, int page, int size) {
        int offset = page * size;
        List<Rental> rentals = rentalRepository.findRentalHistoryByUserId(userId, offset, size);
        long total = rentalRepository.countRentalsByUserId(userId);

        List<RentalResponseDTO> rentalDTOs = rentals.stream().map(rentalMapper::toResponseDTO).toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(rentalDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
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