package org.scoooting.dto;

import lombok.Data;
import org.scoooting.entities.enums.ScootersStatus;

@Data
public class ScootersDTO {

    private Long id;
    private String model;
    private ScootersStatus status;
    private Double latitude;
    private Double longitude;
}
