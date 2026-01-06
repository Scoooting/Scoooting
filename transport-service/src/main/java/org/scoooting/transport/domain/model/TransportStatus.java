package org.scoooting.transport.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransportStatus {

    private Long id;

    private String name; // "AVAILABLE", "IN_USE", "UNAVAILABLE"
}
