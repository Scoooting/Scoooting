package org.scoooting.rental.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("rental_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalStatus {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name; // "ACTIVE", "COMPLETED", "CANCELLED"
}
