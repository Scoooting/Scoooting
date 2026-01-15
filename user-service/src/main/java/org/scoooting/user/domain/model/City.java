package org.scoooting.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class City {

    private Long id;
    private String name;
    private Double centerLatitude;
    private Double centerLongitude;
    private Integer radiusKm;

}
