package org.scoooting.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("cities")
public class City {

    @Id
    private Long id;
    private String name;
    private Double latitudeMin;
    private Double longitudeMin;
    private Double latitudeMax;
    private Double longitudeMax;

}
