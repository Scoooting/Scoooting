package org.scoooting.rental.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalStatus {

    private Long id;

    private String name; // "ACTIVE", "COMPLETED", "CANCELLED"
}
