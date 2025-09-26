package org.scoooting.dto;

import java.util.List;

public record PaginatedRentalsDTO(List<RentalDTO> rentals, Long totalCount) {}

