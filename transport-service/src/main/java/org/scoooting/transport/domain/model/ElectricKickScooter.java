package org.scoooting.transport.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ElectricKickScooter {

    private Long transportId;

    private String model;

    private BigDecimal batteryLevel;

    private Integer maxSpeed;
}