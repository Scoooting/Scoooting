package org.scoooting.transport.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GasMotorcycle {

    private Long transportId;

    private String model;

    private BigDecimal fuelLevel;

    private Integer engineSize;
}
