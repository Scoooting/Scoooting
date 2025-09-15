package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartRentalRequest {
    private Long transportId;
    private Double startLatitude;
    private Double startLongitude;
}
