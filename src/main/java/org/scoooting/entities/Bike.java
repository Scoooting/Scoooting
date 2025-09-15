package org.scoooting.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.BikeStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("bikes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bike {
    @Id
    private Long id;
    private String model;
    private BikeStatus status;
    private Double latitude;
    private Double longitude;
    private Integer gearCount;
    private Boolean isElectric;
}