package org.scoooting.transport.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricBicycle {

    private Long transportId;

    private String model;

    private BigDecimal batteryLevel;

    private Integer gearCount;
}
