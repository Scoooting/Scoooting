package org.scoooting.dto;

import lombok.Data;

@Data
public class ScootersDto {

    private Long id;
    private String model;
    private Double latitude;
    private Double longitude;

}
