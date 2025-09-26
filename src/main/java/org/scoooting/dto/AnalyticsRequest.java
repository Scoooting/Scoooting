package org.scoooting.dto;

import java.time.LocalDateTime;

public record AnalyticsRequest(LocalDateTime startDate, LocalDateTime endDate, Integer minRentals, Integer limit) {}