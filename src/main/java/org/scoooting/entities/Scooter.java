package org.scoooting.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.ScootersStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Table("scooters")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scooter {

    @Id
    private Long id;
    private String model;
    private ScootersStatus status;
    private Double latitude;
    private Double longitude;

}
