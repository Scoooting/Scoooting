package org.scoooting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.BikeStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BikeDTO {
    private Long id;
    private String model;
    private BikeStatus status;
    private Double latitude;
    private Double longitude;
    private Integer gearCount;
    private Boolean isElectric;
}