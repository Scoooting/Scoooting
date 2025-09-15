package org.scoooting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedRentalsDTO {
    private List<RentalDTO> rentals;
    private Long totalCount;
}
