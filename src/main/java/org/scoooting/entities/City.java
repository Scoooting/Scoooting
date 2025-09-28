package org.scoooting.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("cities")
public class City {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 32)
    private String name;

    @NotNull
    private Double latitudeMin;
    @NotNull
    private Double longitudeMin;
    @NotNull
    private Double latitudeMax;
    @NotNull
    private Double longitudeMax;

}
