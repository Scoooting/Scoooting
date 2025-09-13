package org.scoooting.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("scooters")
@Data
public class Scooter {

    @Id
    private Long id;
    private String name;
    private String status;
    private Double latitude;
    private Double longitude;

}
