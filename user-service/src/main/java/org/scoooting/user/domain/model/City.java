package org.scoooting.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class City {

    private Long id;
    private String name;
    private Double centerLatitude;
    private Double centerLongitude;
    private Integer radiusKm;

    public City(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
