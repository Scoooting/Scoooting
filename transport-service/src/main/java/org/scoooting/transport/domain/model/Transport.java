package org.scoooting.transport.domain.model;

import lombok.Builder;
import lombok.Data;
import org.scoooting.transport.domain.model.enums.TransportType;

@Data
@Builder
public class Transport {

    private Long id;

    private TransportType transportType;

    private Long statusId;

    private Long cityId;

    private Double latitude;

    private Double longitude;
}