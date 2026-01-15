package org.scoooting.rental.domain.model;

import lombok.Data;

@Data
public class RentalStatus {

    private Long id;

    private String name; // "ACTIVE", "COMPLETED", "CANCELLED"
}
