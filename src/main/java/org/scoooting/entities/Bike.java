package org.scoooting.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 64)
    private String model;

    @NotBlank
    @Size(max = 32)
    private BikeStatus status;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
    private Integer gearCount;
    private Boolean isElectric;
}