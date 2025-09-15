package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndRentalRequest {
    private Double endLatitude;
    private Double endLongitude;
}
