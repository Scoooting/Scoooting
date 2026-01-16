package org.scoooting.transport.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.transport.domain.model.enums.TransportType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transport {

    private Long id;

    private TransportType transportType;

    private Long statusId;

    private Long cityId;

    private Double latitude;

    private Double longitude;
}