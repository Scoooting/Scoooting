package org.scoooting.dto;

import java.time.LocalDateTime;

public record AreaAnalyticsRequest(Double centerLatitude, Double centerLongitude, Double radiusMeters,
                                  LocalDateTime startDate) {}
